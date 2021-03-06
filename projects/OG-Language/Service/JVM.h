/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

#ifndef __inc_og_language_service_jvm_h
#define __inc_og_language_service_jvm_h

// Start up an embedded JVM, and call methods on the "Main" class

#include <Util/Library.h>
#include <Util/Mutex.h>
#include <Util/Thread.h>

class CJVM {
private:
	mutable CMutex m_oMutex;
	CLibrary *m_poModule;
	JavaVM *m_pJVM;
	JNIEnv *m_pEnv;
	mutable CThread *m_poBusyTask;
	bool m_bRunning;
	CJVM (CLibrary *hModule, JavaVM *pJVM, JNIEnv *pEnv);
	static bool Invoke (JNIEnv *pEnv, const char *pszMethod, const char *pszSignature, ...);
	bool Invoke (const char *pszMethod);
public:
	~CJVM ();
	static CJVM *Create ();
	void Start (bool bAsync = true);
	void Stop (bool bAsync = true);
	bool IsBusy (unsigned long dwTimeout) const;
	bool IsRunning () const;
	bool IsStopped () const;
	void UserConnection (const TCHAR *pszUserName, const TCHAR *pszInputPipe, const TCHAR *pszOutputPipe, const TCHAR *pszLanguageID);
};

#endif /* ifndef __inc_og_language_service_jvm_h */
