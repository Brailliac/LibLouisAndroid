#include "lljni.h"

JAVA_METHOD(
        org_liblouis_Louis, listTables, jobjectArray
) {
    char **tables = lou_listTables();

    int length = 0;
    while (tables[length] != NULL) {
        length++;
    }

    jobjectArray jTables = (*env)->NewObjectArray(env, length, (*env)->FindClass(env, "java/lang/String"), 0);

    jstring str;
    int i;
    for (i = 0; i < length; i++) {
        str = (*env)->NewStringUTF(env, tables[i]);
        (*env)->SetObjectArrayElement(env, jTables, i, str);
    }

    return jTables;
}