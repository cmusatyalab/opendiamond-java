/* -*- c -*- */

/*
 *  The OpenDiamond Platform for Interactive Search
 *  Version 4
 *
 *  Copyright (c) 2007-2009 Carnegie Mellon University
 *  All rights reserved.
 *
 *  This software is distributed under the terms of the Eclipse Public
 *  License, Version 1.0 which can be found in the file named LICENSE.
 *  ANY USE, REPRODUCTION OR DISTRIBUTION OF THIS SOFTWARE CONSTITUTES
 *  RECIPIENT'S ACCEPTANCE OF THIS AGREEMENT
 */

%module OpenDiamond
%include "arrays_java.i"
%include "typemaps.i"
%include "carrays.i"
%include "various.i"

%javaconst(1);


%{
#include "diamond_consts.h"
#include "diamond_types.h"
#include "lib_filter.h"
#include "lib_searchlet.h"
#include "lib_scope.h"

void **create_void_cookie(void) {
  return (void**) malloc(sizeof(void *));
}

void delete_void_cookie(void **c) {
  free(c);
}

void *deref_void_cookie(void **c) {
  return *c;
}

char **create_char_cookie(void) {
  return (char**) malloc(sizeof(char *));
}

char *deref_char_cookie(char **c) {
  if (c == NULL) {
    return NULL;
  } else {
    return *c;
  }
}

void delete_char_cookie(char **c) {
  free(c);
}

void delete_deref_char_cookie(char **c) {
  if (c != NULL) {
    free(*c);
  }
}

unsigned char **create_data_cookie(void) {
  return (unsigned char**) malloc(sizeof(unsigned char *));
}

unsigned char *deref_data_cookie(unsigned char **c) {
  if (c == NULL) {
    return NULL;
  } else {
    return *c;
  }
}

void delete_data_cookie(unsigned char **c) {
  free(c);
}

int get_dev_stats_size(int num_filters) {
  return DEV_STATS_SIZE(num_filters);
}

dev_stats_t *create_dev_stats(int bytes) {
  return malloc(bytes);
}

void delete_dev_stats(dev_stats_t *ds) {
  free(ds);
}

device_session_vars_t **create_session_vars_handle(void) {
  return calloc(1, sizeof(device_session_vars_t *));
}

device_session_vars_t *deref_session_vars_handle(device_session_vars_t **vars) {
  return *vars;
}

void delete_session_vars_handle(device_session_vars_t **vars) {
  free(vars);
}

device_session_vars_t *create_session_vars(int len) {
  device_session_vars_t *vars = calloc(1, sizeof(device_session_vars_t));
  if (vars == NULL) {
    return NULL;
  }

  vars->len = len;
  vars->names = calloc(len, sizeof(char *));
  vars->values = calloc(len, sizeof(double));

  if (vars->names == NULL || vars->values == NULL) {
    free(vars->names);
    free(vars->values);
    free(vars);
    vars = NULL;
  }

  return vars;
}


void delete_session_vars(device_session_vars_t *vars) {
  int i;
  if (vars != NULL) {
    for (i = 0; i < vars->len; i++) {
      free(vars->names[i]);
    }
    free(vars->names);
    free(vars->values);
    free(vars);
  }
}

char **create_string_array(int n) {
  return calloc(n + 1, sizeof(char *));
}

void delete_string_array(char **array) {
  if (array == NULL) {
    return;
  }

  char **current = array;
  while (*current != NULL) {
    free(*current++);
  }

  free(array);
}

char *get_string_element(char **array, int i) {
  return array[i];
}

void set_string_element(char **array, int i, char *string) {
  array[i] = strdup(string);
}


%}

%pragma(java) jniclasscode=%{
  static {
    try {
        System.loadLibrary("opendiamondjava");
    } catch (UnsatisfiedLinkError e) {
      System.err.println("Native code library failed to load. \n" + e);
    }
  }
%}


%array_class(groupid_t, groupidArray);
%array_class(uint32_t, uintArray);
%array_class(ls_dev_handle_t, devHandleArray);
%array_class(double, doubleArray);
%array_class(unsigned char, byteArray);

typedef unsigned int uint32_t;

typedef struct {
  int len;
  char **names;
  doubleArray *values;
} device_session_vars_t;


%include "diamond_consts.h"
%include "diamond_types.h"

#define LSEARCH_NO_BLOCK        0x01

// thanks for using errno
#define EAGAIN      11
#define EWOULDBLOCK EAGAIN


ls_search_handle_t ls_init_search(void);
int ls_terminate_search(ls_search_handle_t handle); // stops search
int ls_set_searchlet(ls_search_handle_t handle, device_isa_t isa_type,
                     char *filter_file_name, char *filter_spec_name);
int ls_add_filter_file(ls_search_handle_t handle, device_isa_t isa_type,
                     char *filter_file_name);
int ls_start_search(ls_search_handle_t handle);

int ls_next_object(ls_search_handle_t handle,
                   ls_obj_handle_t *obj_handle,
                   int flags);
int ls_release_object(ls_search_handle_t handle,
                      ls_obj_handle_t obj_handle);
int ls_set_blob(ls_search_handle_t handle,
		char *filter_name, int blob_len, char *BYTE);

int ls_get_dev_list(ls_search_handle_t handle, devHandleArray *handle_list,
		    int *INOUT);
int ls_get_dev_stats(ls_search_handle_t handle,
                     ls_dev_handle_t dev_handle,
                     dev_stats_t *dev_stats, int *INOUT);

int ls_get_dev_session_variables(ls_search_handle_t handle,
				 ls_dev_handle_t dev_handle,
				 device_session_vars_t **INOUT);
int ls_set_dev_session_variables(ls_search_handle_t handle,
				 ls_dev_handle_t dev_handle,
				 device_session_vars_t *vars);


typedef	void *	lf_obj_handle_t;
typedef unsigned int  size_t;
int lf_ref_attr(lf_obj_handle_t ohandle, const char *name,
		size_t *OUTPUT, unsigned char **data);
int lf_first_attr(lf_obj_handle_t ohandle, char **name,
		size_t *OUTPUT, unsigned char **data, void **cookie);
int lf_next_attr(lf_obj_handle_t ohandle, char **name,
		size_t *OUTPUT, unsigned char **data, void **cookie);

int ls_define_scope(ls_search_handle_t handle);

int ls_set_push_attributes(ls_search_handle_t handle,
			   const char **attributes);
int ls_get_objectid(ls_search_handle_t handle, ls_obj_handle_t obj_handle,
		    const char **objectid);
int ls_reexecute_filters(ls_search_handle_t handle,
			 const char *objectid, const char **attributes,
			 ls_obj_handle_t *obj_handle);
const char *ls_get_dev_name(ls_search_handle_t handle,
			    ls_dev_handle_t dev_handle);

void **create_void_cookie(void);
void delete_void_cookie(void **c);
void *deref_void_cookie(void **c);
char **create_char_cookie(void);
char *deref_char_cookie(char **c);
void delete_char_cookie(char **c);
void delete_deref_char_cookie(char **c);
unsigned char **create_data_cookie(void);
byteArray *deref_data_cookie(unsigned char **c);
void delete_data_cookie(unsigned char **c);
int get_dev_stats_size(int num_filters);
dev_stats_t *create_dev_stats(int bytes);
void delete_dev_stats(dev_stats_t *ds);

device_session_vars_t **create_session_vars_handle(void);
device_session_vars_t *deref_session_vars_handle(device_session_vars_t **vars);
void delete_session_vars_handle(device_session_vars_t **vars);

device_session_vars_t *create_session_vars(int len);
void delete_session_vars(device_session_vars_t *vars);

char **create_string_array(int n);
void delete_string_array(char **array);
char *get_string_element(char **array, int i);
void set_string_element(char **array, int i, char *string);
