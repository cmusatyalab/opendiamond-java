/* -*- c -*- */

/*
 *  The OpenDiamond Platform for Interactive Search
 *  Version 3
 *
 *  Copyright (c) 2007 Carnegie Mellon University
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
#include "lib_dconfig.h"
#include "lib_filter.h"
#include "lib_searchlet.h"
#include "lib_scope.h"

#define MAX_DEV_GROUPS          64

typedef struct device_handle {
        struct device_handle *          next;
        uint32_t                        dev_id;
        char *                          dev_name;
        groupid_t                       dev_groups[MAX_DEV_GROUPS];
        int                             num_groups;
        unsigned int                    flags;
        void *                          dev_handle;
        int                             ver_no;
        time_t                          start_time;
        int                             remain_old;
        int                             remain_mid;
        int                             remain_new;
        float                           done;
        float                           delta;
        float                           prate;
        int                             obj_total;
        float                           cur_credits;    /* credits for current iteration */
        int                             credit_incr;    /* incremental credits to add */
        int                             serviced;       /* times data removed */
        struct                          search_context *        sc;
} device_handle_t;


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

void get_ipv4addr_from_dev_handle(ls_dev_handle_t dev, signed char addr[]) {
  device_handle_t *dhandle = (device_handle_t *) dev;
  int a = dhandle->dev_id;
  *((int *) addr) = a;
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

%include "diamond_consts.h"
 //%include "diamond_types.h"

typedef	void *	ls_obj_handle_t;
typedef	void *	ls_search_handle_t;
typedef	void *	ls_dev_handle_t;

typedef enum {
    DEV_ISA_UNKNOWN = 0,
    DEV_ISA_IA32,
    DEV_ISA_IA64,
    DEV_ISA_XSCALE,
} device_isa_t;


typedef struct dev_stats {
	int		ds_objs_total;	   	/* total objs in search  */
	int		ds_objs_processed;	/* total objects by device */
	int		ds_objs_dropped;	/* total objects dropped */
	int		ds_objs_nproc;		/* objs not procced at disk */
	int		ds_system_load;		/* average load on  device??? */
	rtime_t	ds_avg_obj_time;	/* average time per objects */
	int		ds_num_filters; 	/* number of filters */
	filter_stats_t	ds_filter_stats[0];	/* list of filter */
} dev_stats_t;


#define LSEARCH_NO_BLOCK        0x01

// thanks for using errno
#define EAGAIN      11
#define EWOULDBLOCK EAGAIN


int nlkup_first_entry(char **name, void **cookie);
int nlkup_next_entry(char **name, void **cookie);

typedef unsigned int uint32_t;

%array_class(groupid_t, groupidArray);
%array_class(uint32_t, uintArray);

int nlkup_lookup_collection(char *name, int *INOUT, groupidArray *gids);
int glkup_gid_hosts(groupid_t gid, int *INOUT, uintArray *hostids);

ls_search_handle_t ls_init_search(void);
int ls_terminate_search(ls_search_handle_t handle); // stops search
int ls_set_searchlist(ls_search_handle_t handle, int num_groups,
                      groupidArray *glist);
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
		char *filter_name, int blob_len, char *blob_data);

%array_class(ls_dev_handle_t, devHandleArray);
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
int lf_next_block(lf_obj_handle_t obj_handle, int num_blocks,
			size_t *OUTPUT, unsigned char **data);
int lf_ref_attr(lf_obj_handle_t ohandle, const char *name,
		size_t *OUTPUT, unsigned char **data);
int lf_first_attr(lf_obj_handle_t ohandle, char **name,
		size_t *OUTPUT, unsigned char **data, void **cookie);
int lf_next_attr(lf_obj_handle_t ohandle, char **name,
		size_t *OUTPUT, unsigned char **data, void **cookie);

int ls_define_scope(void);


%array_class(unsigned char, byteArray);

void **create_void_cookie(void);
void delete_void_cookie(void **c);
void *deref_void_cookie(void **c);
char **create_char_cookie(void);
char *deref_char_cookie(char **c);
void delete_char_cookie(char **c);
unsigned char **create_data_cookie(void);
byteArray *deref_data_cookie(unsigned char **c);
void delete_data_cookie(unsigned char **c);
int get_dev_stats_size(int num_filters);
dev_stats_t *create_dev_stats(int bytes);
void delete_dev_stats(dev_stats_t *ds);
void get_ipv4addr_from_dev_handle(ls_dev_handle_t dev, signed char addr[]);

%array_class(double, doubleArray);

device_session_vars_t **create_session_vars_handle(void);
device_session_vars_t *deref_session_vars_handle(device_session_vars_t **vars);
void delete_session_vars_handle(device_session_vars_t **vars);

device_session_vars_t *create_session_vars(int len);
void delete_session_vars(device_session_vars_t *vars);

char *get_string_element(char **array, int i);
void set_string_element(char **array, int i, char *string);

typedef struct {
  int len;
  char **names;
  doubleArray *values;
} device_session_vars_t;
