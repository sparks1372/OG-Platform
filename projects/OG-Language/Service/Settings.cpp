/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#include "stdafx.h"

// Runtime configuration options

#include "Settings.h"
#include <Util/File.h>
#include <Util/Process.h>
#include <Util/Quote.h>

LOGGING(com.opengamma.language.service.Settings);

#ifndef DEFAULT_CONNECTION_TIMEOUT
# define DEFAULT_CONNECTION_TIMEOUT	3000	/* 3s default */
#endif /* ifndef DEFAULT_CONNECTION_TIMEOUT */
#ifndef DEFAULT_BUSY_TIMEOUT
# define DEFAULT_BUSY_TIMEOUT		2000	/* 2s default */
#endif /* ifndef DEFAULT_BUSY_TIMEOUT */
#ifndef DEFAULT_IDLE_TIMEOUT
# define DEFAULT_IDLE_TIMEOUT		300000	/* 5m default */
#endif /* ifndef DEFAULT_IDLE_TIMEOUT */
#ifndef DEFAULT_JVM_LIBRARY
# ifdef _WIN32
#  define DEFAULT_JVM_LIBRARY		TEXT ("jvm.dll")
# else /* ifdef _WIN32 */
#  define DEFAULT_JVM_LIBRARY		TEXT ("jvm.so")
# endif /* ifdef _WIN32 */
#endif /* ifndef DEFAULT_JVM_LIBRARY */
#ifndef DEFAULT_LOG_CONFIGURATION
# define DEFAULT_LOG_CONFIGURATION	NULL
#endif /* ifndef DEFAULT_LOG_CONFIGURATION */
#ifdef _WIN32
# ifndef DEFAULT_SDDL
#  define DEFAULT_SDDL				NULL
# endif /* ifndef DEFAULT_SDDL */
#endif /* ifdef _WIN32 */
#ifndef DEFAULT_SERVICE_NAME
# define DEFAULT_SERVICE_NAME		TEXT ("OpenGammaLanguageAPI")
#endif /* ifndef DEFAULT_SERVICE_NAME */
#ifndef DEFAULT_CONNECTION_PIPE
# ifndef DEFAULT_PIPE_NAME
#  define DEFAULT_PIPE_NAME			TEXT ("Connection")
# endif /* ifndef DEFAULT_PIPE_NAME */
# ifdef _WIN32
#  define DEFAULT_CONNECTION_PIPE	TEXT ("\\\\.\\pipe\\") DEFAULT_SERVICE_NAME TEXT ("-") DEFAULT_PIPE_NAME
# else /* ifdef _WIN32 */
#  ifndef DEFAULT_PIPE_FOLDER
#   define DEFAULT_PIPE_FOLDER		TEXT ("/var/run/OG-Language/")
#  endif /* ifndef DEFAULT_PIPE_FOLDER */
#  define DEFAULT_CONNECTION_PIPE	DEFAULT_PIPE_FOLDER DEFAULT_PIPE_NAME TEXT (".sock")
# endif /* ifdef _WIN32 */
#endif /* ifndef DEFAULT_CONNECTION_PIPE */

const TCHAR *ServiceDefaultConnectionPipe () {
	return DEFAULT_CONNECTION_PIPE;
}

const TCHAR *ServiceDefaultServiceName () {
	return DEFAULT_SERVICE_NAME;
}

class CJvmLibraryDefault : public CAbstractSettingProvider {
private:
#ifdef _WIN32
	static BOOL IsValidLibrary (PCTSTR pszLibrary) {
		DWORD dwAttrib = GetFileAttributes (pszLibrary);
		if (dwAttrib == INVALID_FILE_ATTRIBUTES) return FALSE;
		// Try and load to verify that the library is the same bittage as this process
		HMODULE hModule = LoadLibrary (pszLibrary);
		if (hModule) {
			FreeLibrary (hModule);
			return TRUE;
		} else {
			DWORD dwError = GetLastError ();
			if (dwError == ERROR_BAD_EXE_FORMAT) {
#if defined (_M_IX86)
				LOGERROR (TEXT ("Found ") << pszLibrary << TEXT (" but it is not a 32-bit Windows module - try the 64-bit service"));
#elif defined (_M_X64)
				LOGERROR (TEXT ("Found ") << pszLibrary << TEXT (" but it is not a 64-bit Windows module - try the 32-bit service"));
#else
#error "Need correct error message for processor"
#endif
			} else {
				LOGWARN (TEXT ("Found ") << pszLibrary << TEXT (" but couldn't load, error ") << dwError);
			}
			return FALSE;
		}
	}
#endif

#define MAX_ENV_LEN 32767
	TCHAR *CalculateString () const {
		TCHAR *pszLibrary = NULL;
#ifdef _WIN32
		TCHAR *pszPath = new TCHAR[MAX_ENV_LEN];
		if (!pszPath) {
			LOGERROR ("Out of memory");
			return NULL;
		}
		DWORD dwPath = GetEnvironmentVariable (TEXT ("JAVA_HOME"), pszPath, MAX_ENV_LEN);
		if (dwPath != 0) {
			size_t cb = dwPath + 20; // \bin\server\jvm.dll +\0
			pszLibrary = new TCHAR[cb];
			if (!pszLibrary) {
				delete pszPath;
				LOGERROR ("Out of memory");
				return NULL;
			}
			StringCchPrintf (pszLibrary, cb, TEXT ("%s\\bin\\server\\jvm.dll"), pszPath);
			if (IsValidLibrary (pszLibrary)) {
				LOGINFO (TEXT ("Default jvm.dll ") << pszLibrary << TEXT (" found from JAVA_HOME"));
			} else {
				StringCchPrintf (pszLibrary, cb, TEXT ("%s\\bin\\client\\jvm.dll"), pszPath);
				if (IsValidLibrary (pszLibrary)) {
					LOGINFO (TEXT ("Default jvm.dll ") << pszLibrary << TEXT (" found from JAVA_HOME"));
				} else {
					LOGWARN (TEXT ("JAVA_HOME set but bin\\<server|client>\\jvm.dll doesn't exist"));
					delete pszLibrary;
					pszLibrary = NULL;
				}
			}
		}
		if (pszLibrary == NULL) {
			DWORD dwPath = GetEnvironmentVariable (TEXT ("PATH"), pszPath, MAX_ENV_LEN);
			if (dwPath != 0) {
				TCHAR *nextToken;
				TCHAR *psz = _tcstok_s (pszPath, TEXT (";"), &nextToken);
				while (psz != NULL) {
					size_t cb = dwPath + 27; // MAX( \java.exe +\0 , \server\jvm.dll +\0 , \..\jre\bin\server\jvm.dll + \0)
					pszLibrary = new TCHAR[cb];
					if (!pszLibrary) {
						delete pszPath;
						LOGERROR ("Out of memory");
						return NULL;
					}
					StringCchPrintf (pszLibrary, cb, TEXT ("%s\\java.exe"), psz);
					LOGDEBUG (TEXT ("Looking for ") << pszLibrary);
					if (!IsValidLibrary (pszLibrary)) {
						delete pszLibrary;
						pszLibrary = NULL;
						psz = _tcstok_s (NULL, TEXT (";"), &nextToken);
						continue;
					}
					LOGDEBUG (TEXT ("Default Java executable ") << pszLibrary << TEXT (" found from PATH"));
					StringCchPrintf (pszLibrary, cb, TEXT ("%s\\server\\jvm.dll"), psz);
					LOGDEBUG (TEXT ("Looking for ") << pszLibrary);
					if (IsValidLibrary (pszLibrary)) {
						LOGINFO (TEXT ("Default jvm.dll ") << pszLibrary << TEXT (" found from PATH"));
						break;
					}
					StringCchPrintf (pszLibrary, cb, TEXT ("%s\\..\\jre\\bin\\server\\jvm.dll"), psz);
					LOGDEBUG (TEXT ("Looking for ") << pszLibrary);
					if (IsValidLibrary (pszLibrary)) {
						LOGINFO (TEXT ("Default jvm.dll ") << pszLibrary << TEXT (" found from PATH"));
						break;
					}
					StringCchPrintf (pszLibrary, cb, TEXT ("%s\\client\\jvm.dll"), psz);
					LOGDEBUG (TEXT ("Looking for ") << pszLibrary);
					if (IsValidLibrary (pszLibrary)) {
						LOGINFO (TEXT ("Default jvm.dll ") << pszLibrary << TEXT (" found from PATH"));
						break;
					}
					StringCchPrintf (pszLibrary, cb, TEXT ("%s\\..\\jre\\bin\\client\\jvm.dll"), psz);
					LOGDEBUG (TEXT ("Looking for ") << pszLibrary);
					if (IsValidLibrary (pszLibrary)) {
						LOGINFO (TEXT ("Default jvm.dll ") << pszLibrary << TEXT (" found from PATH"));
						break;
					}
					delete pszLibrary;
					pszLibrary = NULL;
					psz = _tcstok_s (NULL, TEXT (";"), &nextToken);
					continue;
				}
			}
		}
		delete pszPath;
#else
		// TODO: Is there anything reasonably generic? The path on my Linux box included a processor (e.g. amd64) reference
#endif
		if (pszLibrary == NULL) {
			LOGDEBUG ("No default JVM libraries found on JAVA_HOME or PATH");
			pszLibrary = _tcsdup (DEFAULT_JVM_LIBRARY);
		}
		return pszLibrary;
	}
};

static CJvmLibraryDefault g_oJvmLibraryDefault;

const TCHAR *CSettings::GetJvmLibrary () const {
	return GetJvmLibrary (&g_oJvmLibraryDefault);
}

const TCHAR *CSettings::GetConnectionPipe () const {
	return GetConnectionPipe (ServiceDefaultConnectionPipe ());
}

unsigned long CSettings::GetConnectionTimeout () const {
	return GetConnectionTimeout (DEFAULT_CONNECTION_TIMEOUT);
}

class CJarPathDefault : public CAbstractSettingProvider {
protected:
#define CLIENT_JAR_NAME		TEXT ("client.jar")
#define CLIENT_JAR_LEN		10
	TCHAR *CalculateString () const {
		TCHAR *pszJarPath = NULL;
		// Scan backwards from the module to find a path which has Client.jar in. This works if all of the
		// JARs and DLLs are in the same folder, but also in the case of a build system where we have sub-folders
		// for the different configurations/platforms.
		TCHAR szPath[256 + CLIENT_JAR_LEN]; // Guarantee room for Client.jar at the end
		if (CProcess::GetCurrentModule (szPath, 256)) {
			LOGDEBUG (TEXT ("Module = ") << szPath);
			TCHAR *pszEnd = _tcsrchr (szPath, PATH_CHAR);
			while (pszEnd) {
#ifdef _DEBUG
				*pszEnd = 0;
				LOGDEBUG (TEXT ("Testing path ") << szPath);
				*pszEnd = PATH_CHAR;
#endif
				memcpy (pszEnd + 1, CLIENT_JAR_NAME, (CLIENT_JAR_LEN + 1) * sizeof (TCHAR));
#ifdef _WIN32
				HANDLE hFile = CreateFile (szPath, GENERIC_READ, FILE_SHARE_READ, NULL, OPEN_EXISTING, 0, NULL);
				if (hFile == INVALID_HANDLE_VALUE) {
#else
				int nFile = open (szPath, O_RDONLY);
				if (nFile <= 0) {
#endif
					int ec = GetLastError ();
					if (ec != ENOENT) {
						LOGWARN (TEXT ("Couldn't scan for ") << szPath << TEXT (", error ") << ec);
						break;
					}
					*pszEnd = 0;
				} else {
#ifdef _WIN32
					CloseHandle (hFile);
#else
					close (nFile);
#endif
					*pszEnd = 0;
					LOGINFO (TEXT ("Found path ") << szPath << TEXT (" containing ") << CLIENT_JAR_NAME);
					pszJarPath = _tcsdup (szPath);
					break;
				}
				pszEnd = _tcsrchr (szPath, PATH_CHAR);
			}
			if (!pszJarPath) {
				LOGWARN (TEXT ("Couldn't find client library Jar on module path"));
				pszJarPath = _tcsdup (TEXT ("."));
			}
		} else {
			pszJarPath = _tcsdup (TEXT ("."));
		}
		return pszJarPath;
	}
};

static CJarPathDefault g_oJarPathDefault;

const TCHAR *CSettings::GetJarPath () const {
	return GetJarPath (&g_oJarPathDefault);
}

const TCHAR *CSettings::GetAnnotationCache () const {
	return GetAnnotationCache (GetJarPath ());
}

unsigned long CSettings::GetBusyTimeout () const {
	return GetBusyTimeout (DEFAULT_BUSY_TIMEOUT);
}

const TCHAR *CSettings::GetLogConfiguration () const {
	return GetLogConfiguration (DEFAULT_LOG_CONFIGURATION);
}

unsigned long CSettings::GetIdleTimeout () const {
	return GetIdleTimeout (DEFAULT_IDLE_TIMEOUT);
}

const TCHAR *CSettings::GetServiceName () const {
	return GetServiceName (ServiceDefaultServiceName ());
}

#ifdef _WIN32
const TCHAR *CSettings::GetServiceSDDL () const {
	return GetServiceSDDL (DEFAULT_SDDL);
}
#endif /* ifdef _WIN32 */
