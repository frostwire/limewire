#include <stdlib.h>
#include <stdio.h>
#include <string.h>

#define XML_TEST_FILE "version.xml"

#define MIN_OFFICIAL_STYLE 1

#define DEBUG_NOW

#ifdef WIN32
#define CASE_INSENSITIVE_COMPARE	strnicmp
#else
#define CASE_INSENSITIVE_COMPARE	_strncasecmp
#endif

#ifndef WIN32

typedef long long __int64;
#define _atoi64(s) atoll(s)

#endif

/* Constants used in the special-case parsing of <![CDATA[Arbitrary String Here]]> */
#define CDATA_TAG "<![CDATA["
#define CDATA_TAG_LENGTH 9

/* 8 kb minus one byte */
#define MAX_ATTRIBUTE_LENGTH 8191

#define OS_NAME "windows"



/* States of our xml parser state machine */
enum {
    OUTSIDE_MSG_TAG,
    INSIDE_MSG_OPENING_TAG,
    INSIDE_MSG_TAG_BODY
};

struct download_data_t {
	download_data_t()
	{ memset( this, 0, sizeof(*this) ); }

    int update_id;
    int size;

    int os_match;
    int style;
    int free;
    int pro;
    int version_number[4]; /* major, minor, service, revision */
    char *urn;
    char *url;
    char *uname;
    char *icommand;
};


/* Initializes the values in a download_data struct */
static void init_download_data(download_data_t *d_ptr) {
    int i;

    d_ptr->update_id = -1;
    d_ptr->size = 0;

    d_ptr->os_match = 1; /* The os matches by default. */
    d_ptr->style    = -1; /* -1 indicates an error that no style was specified */
    d_ptr->free     = 0;
    d_ptr->pro      = 0;
    for(i=0; i < 4; ++i)
        d_ptr->version_number[i] = -1;

    /* Make strings zero-length */
    if(d_ptr->urn != NULL)       d_ptr->urn[0]      = (char) '\0';
    if(d_ptr->url != NULL)       d_ptr->url[0]      = (char) '\0';
    if(d_ptr->uname != NULL)     d_ptr->uname[0]    = (char) '\0';
    if(d_ptr->icommand != NULL)  d_ptr->icommand[0] = (char) '\0';
}

/* Frees pointers if they're not null */
static void free_if_not_null(void *ptr) {
#ifdef DEBUG
printf("Freeing %p\n", ptr);
#endif
	if (ptr != NULL)
		free(ptr);
}

/* Frees all of the internal pointers in a download_data struct */
static void free_download_data(download_data_t *d_ptr) {
#ifdef DEBUG
printf("Freeing download data\n");
write_version(stdout, d_ptr);
#endif
    free_if_not_null(d_ptr->urn);
    free_if_not_null(d_ptr->url);
    free_if_not_null(d_ptr->uname);
    free_if_not_null(d_ptr->icommand);
}


/* Allocates and copies src.  If the length of src is zero, 
 * then *dest_ptr is instead made NULL. */
static int copy_string_or_null_out(char **dest_ptr, const char *src) {
    size_t length;
    
	if (src == NULL || *dest_ptr == src)
		return 0;  /* Bad source */	

    free_if_not_null(*dest_ptr);

    length = strlen(src);
 
    if (length == 0) {
        *dest_ptr = NULL;
        return 0;
    }

    *dest_ptr = (char *) malloc (length + 1);
    strcpy(*dest_ptr, src);
#ifdef DEBUG
    printf("Setting string %p from %p :%s\n", *dest_ptr, src, *dest_ptr);
#endif
	return 1;
}

/* Coppies src to dest, being careful not to duplicate pointers */
static int copy_download_data(download_data_t *dest, download_data_t *src) {
	free_download_data(dest);
	
	/* Copy everything, including duplicating pointers */
	*dest = *src;
	
	/* Get rid of duplicate pointers */
	dest->urn      = NULL;
	dest->uname    = NULL;
	dest->url      = NULL;
	dest->icommand = NULL;

	/* copy strings to prevent double-frees or data corruption */
   if (!copy_string_or_null_out(&dest->urn,      src->urn))      return 0;
   if (!copy_string_or_null_out(&dest->uname,    src->uname))    return 0;
   if (!copy_string_or_null_out(&dest->url,      src->url))      return 0;
   if (!copy_string_or_null_out(&dest->icommand, src->icommand)) return 0;
}

int write_version(FILE *out, download_data_t *d_ptr) {
    if (out == NULL || d_ptr == NULL)
        return 0;

    fprintf(out, "Version %d.%d.%d_%d\nupdate_id : %d\nsize : %d\n",
        d_ptr->version_number[0], d_ptr->version_number[1], d_ptr->version_number[2],
        d_ptr->version_number[3], d_ptr->update_id, d_ptr->size);
    fprintf(out, "urn : %s\nurl : %s\nuname : %s\nicommand : %s\n",
        d_ptr->urn, d_ptr->url, d_ptr->uname, d_ptr->icommand);

    return 1;
}


/* Returns 0 if a == b.  Returns < 0 if a < b.  Returns > 0 if a > b. */
static int compare_versions(const download_data_t *a, const download_data_t *b) {
    int comparison;
    int i;    

    comparison = a->update_id - b->update_id;
    if (comparison != 0)
        return comparison;

    for(i=0; i < 4; ++i) {
        comparison = a->version_number[i] - b->version_number[i];
        if (comparison != 0)
            return comparison;
    }

    return 0;
}


/* Gets the next token in the xml string, returning a pointer to its start,
 * and using a passed int pointer to also return the length of the token.
 * This does not allocate any new memory nor does it modify the xml string */
int next_token(const char *xml, const char **token, int *token_length_ptr) {
    int in_double_quotes;
    int in_single_quotes;

    /* First of all, move xml to the next non-whitespace character */
    xml += strspn(xml, " \t\n\r");

    if (xml[0] == '\0')  {
        *token_length_ptr = -1;
        *token = NULL;
        return 1;
    }

    /* Special case for <![CDATA[ tags */
    if (strncmp(CDATA_TAG, xml, CDATA_TAG_LENGTH) == 0) {
        char *end_tag = strstr(xml, "]]>");
        if (end_tag == NULL) {
            *token_length_ptr = -1;
            *token = NULL;
            return 0;
        }
        *token_length_ptr = end_tag - xml + 3;
        *token = xml;
        return 1;
    }

    /* Scan for breaking whitespace OR an escaped character OR 
     * a double-quote character */
    in_double_quotes = 0;
    in_single_quotes = 0;
    *token_length_ptr = 1;
    while(1) {
        if (in_double_quotes) {
            *token_length_ptr += strcspn(&xml[*token_length_ptr], "\"\\");
        } else if (in_single_quotes) {
            *token_length_ptr += strcspn(&xml[*token_length_ptr], "\'\\");
        } else {
            *token_length_ptr += strcspn(&xml[*token_length_ptr], "<> \t\n\r\"\'");
        }

        switch(xml[*token_length_ptr]) { 
			case '\0':
                /* Don't run off the end of the string */
                *token = xml;
                return 1;
            case '<':
                /* New tag inside */
                *token = xml;
                return 1;
            case '>':
                /* Close current tag */
                ++*token_length_ptr;
                *token = xml;
                return 1;
            case '\\':
            /* Skip escaped characters */
                *token_length_ptr += 2;
                break;
            case '\"':
            /* Deal with double quotes */
                ++*token_length_ptr;
                if (in_double_quotes) {
                   *token = xml;
                   return 1;
                } 
                in_double_quotes = 1;
                break;
            case '\'':
            /* Deal with single quotes */
                ++*token_length_ptr;
                if (in_single_quotes) {
                    *token = xml;
                    return 1;
                }
                in_single_quotes = 1;
                break;
            default:
                *token = xml;
                return 1;
        }
    }
}

/* Extracts an int from an attribute */
int set_int(int *i_ptr, const char *attribute) {
    char *start;

#ifdef DEBUG_NOW
    char print_buf[512];
    strncpy(print_buf, attribute, 10);
    printf("Setting int :%s\n", print_buf);
#endif

    start = strchr(attribute, '=');
    if (start == NULL)
        return 0;

	/* Skip over single or double quotes */
    start += 2;
    if (start[-1] != '\'' && start[-1] != '\"')
        return 0;

    *i_ptr = atoi(start);

#ifdef DEBUG_NOW
	printf("Set int to %d\n", *i_ptr);
#endif

    return 1;
}

/* Sets an int, if the beginning of the attribute matches name */
/* A mis-match of name and the attribute name is not considered a failure */
int set_int_if_name(int *i_ptr, const char *attribute, const char *name) {
    /* Return success early if there is a name mis-match */
    if (strncmp(attribute, name, strlen(name)) != 0)
        return 1;
    return set_int(i_ptr, attribute);
}

/* Extracts a string from an attribute.
 * Fails if *destination is NULL, or if attribute is not a well-formed xml attribute. */
int set_string(char **destination_ptr, const char *attribute, int attribute_length) {
    const char *last_char_ptr;
    int string_length;

    if (*destination_ptr == NULL)
        return 0;

    if(attribute_length > MAX_ATTRIBUTE_LENGTH) {
        if (realloc(*destination_ptr, attribute_length + 1) == NULL)
            return 0; /* Out of memory */
        (*destination_ptr)[attribute_length] = (char) '\0';
    }

    last_char_ptr = attribute + attribute_length - 1;

    /* Advance attribute pointer to after equal sign and opening quote*/
    attribute = memchr(attribute, '=', attribute_length);
    if (attribute == NULL)
        return 0;
     attribute+=2;
    
    /* make sure string begins and ends with quotes */
    if (attribute[-1] != '\"' && attribute[-1] != '\'')
        return 0;
    if (attribute[-1] != *last_char_ptr)
        return 0;

    /* Don't include the closing quote in string_length.           */
    /* Othertwise string_length would be last_char_ptr-attribute+1 */
    string_length = (int) (last_char_ptr-attribute);

    strncpy(*destination_ptr, attribute, string_length);
    (*destination_ptr)[string_length] = (char) '\0';

    printf("Set string to be %s\n", *destination_ptr);

    return 1;
}

/* Sets a string, if the beginning of the attribute matches name */
/* A mis-match of name and the attribute name is not considered a failure */
int set_string_if_name(char **destination, const char *attribute, int attribute_length, const char *name) {
    /* Return success early if there is a name mis-match */
#ifdef DEBUG
    char print_buf[512];
    strncpy(print_buf, attribute, attribute_length);
    print_buf[attribute_length] = (char) '\0';
    printf("Set string if name %s : %s\n", name, print_buf);
#endif
    
    if (strncmp(attribute, name, strlen(name)) != 0)
        return 1;
    return set_string(destination, attribute, attribute_length);
}

/* Sets the version number on the passed version_t */
int set_version_number(download_data_t *d_ptr, const char *version_attr, int version_attr_length) {
    int i;
    const char *last_char_ptr;

#ifdef DEBUG
    char print_buf[512];
    strncpy(print_buf, version_attr, version_attr_length);
    print_buf[version_attr_length] = (char) '\0';
    printf("Setting version number :%s\n", print_buf);
#endif

    if (strncmp("for=", version_attr, 4) != 0)
        return 0; 

    last_char_ptr = version_attr + version_attr_length - 1;

    /* Check that it begins and ends with quotes */
    if (version_attr[4] != '\"' && version_attr[4] != '\'')
        return 0;
    if (version_attr[4] != *last_char_ptr)
        return 0;

    /* skip to after the quote */
    version_attr += 5;

    /* Extract up to 4 numbers from the version string */
    for(i=0; i < 4; ++i) {
        d_ptr->version_number[i] = atoi(version_attr); 
        version_attr += strspn(version_attr, "0123456789");
        version_attr += strcspn(version_attr, "01234566789 \t\"");
        if (version_attr == last_char_ptr)
            break;
    }

    /* Check for expected character after quotes */
    switch (version_attr[1]) {
        case ' ':
        case '>':
        case '\t':   
        case '\r':
        case '\n':
            return 1;
    }

    return 0;
}


/* Sets a version number, if the beginning of the attribute matches name */
/* A mis-match of name and the attribute name is not considered a failure */
int set_version_if_name(download_data_t *d_ptr, const char *attribute, int attribute_length, const char *name) {
    /* Return success early if there is a name mis-match */
    
    if (strncmp(attribute, name, strlen(name)) != 0)
        return 1;
    return set_version_number(d_ptr, attribute, attribute_length);
}


/* If candidate is newer than current, this function replaces current with candidate */
/* and nulls out the pointers in candidate                                           */
static void update_latest_version(download_data_t *current, download_data_t *candidate) {
#ifdef DEBUG
   printf("Version compare: %d\n", compare_versions(candidate, current));
#endif
   if (compare_versions(candidate, current) <= 0)
       return;

#ifdef DEBUG
   printf("Updating\n");
   write_version(stdout, current);
   printf("to\n");
   write_version(stdout, candidate);
#endif

   copy_download_data(current, candidate);
   
#ifdef DEBUG
	printf("results in\n");
	write_version(stdout, current);
#endif
}


int process_message_tag(const char **xml_ptr, download_data_t *official_ptr,
  download_data_t *beta_ptr, download_data_t *candidate_ptr, int update_id) {
    int token_length;

#ifdef DEBUG
    printf("Handling <msg tag\n");
#endif

    /* clear data */
    init_download_data(candidate_ptr);
    candidate_ptr->update_id = update_id;
#ifdef DEBUG_NOW
printf("Handling msg tag, candidate.update_id = %d\n", candidate_ptr->update_id);
#endif

    /* Get token length again */
    if(!next_token(*xml_ptr, xml_ptr, &token_length))
        return 0;

    /* loop over tokens */
    while(*xml_ptr != NULL) {
#ifdef DEBUG
        char print_buf[1024];
        strncpy(print_buf, *xml_ptr, token_length);
        print_buf[token_length] = (char) '\0';
        printf("Token : %s\n", print_buf);
#endif
        if (token_length == 1 && (*xml_ptr)[0] == '>') {
            printf("Close <msg > tag\n");
            /* Close of msg tag */
            /* Sanity checks */
            if (!candidate_ptr->os_match)
                return 1; /* wrong os... skip to next <msg tag */
            if (candidate_ptr->style == -1) 
                return 1; /* No style read... skip to next <msg tag */
            if (candidate_ptr->urn[0] == '\0')
                return 1; /* No urn... skip to next <msg tag */
            if ((!candidate_ptr->free) && candidate_ptr->pro)
                return 1; /* Pro-only update message */
 
            /* See if we should update either version */
            if (candidate_ptr->style >= MIN_OFFICIAL_STYLE) {
                update_latest_version(official_ptr, candidate_ptr);
            } else {
                update_latest_version(beta_ptr, candidate_ptr);
            }
            /* successful completion of parsig <msg > tag */
            return 1;
        } else if (token_length == 4 && strncmp(*xml_ptr, "<msg", 4) == 0) {
            /* Illegal recursion of <msg > tags */
#ifdef DEBUG
            printf("Illegal <msg recursion\n");
#endif
            return 0;
        } else if (strncmp(*xml_ptr, "os=", 3) == 0) {
            /* check for proper quotes */
            if ((*xml_ptr)[3] != '\"' && (*xml_ptr)[3] != '\'') return 0;
            //if ((*xml_ptr)[3] != (*xml_ptr)[token_length-1])    return 0;
            /* os="" is 5 characters long */
            if (token_length == (5 + strlen(OS_NAME)) && 0 == 
			  CASE_INSENSITIVE_COMPARE
			  ((*xml_ptr)+4, OS_NAME, strlen(OS_NAME))
			)
                candidate_ptr->os_match = 2; /* Better than update for all oses */
            else
                candidate_ptr->os_match = 0; /* False match for os name */
        } else {
            /* Laundry list of attributes to set */
            if(!set_int_if_name(&candidate_ptr->size, *xml_ptr, "size="))
            	return 0;
            if(!set_int_if_name(&candidate_ptr->style, *xml_ptr, "style="))
            	return 0;
            if(!set_int_if_name(&candidate_ptr->free, *xml_ptr, "free="))
            	return 0;
            if(!set_int_if_name(&candidate_ptr->pro, *xml_ptr, "pro="))
            	return 0;
            if(!set_string_if_name(&candidate_ptr->urn, *xml_ptr, token_length, "urn="))
            	return 0;
            if(!set_string_if_name(&candidate_ptr->url, *xml_ptr, token_length, "url="))
            	return 0;
            if(!set_string_if_name(&candidate_ptr->uname, *xml_ptr, token_length, "uname="))
            	return 0;
            if(!set_string_if_name(&candidate_ptr->icommand, *xml_ptr, token_length, "icommand="))
            	return 0;
            if(!set_version_if_name(candidate_ptr, *xml_ptr, token_length, "for="))
            	return 0;
#ifdef DEBUG
            printf("Done with laundary list\n");
            write_version(stdout, candidate_ptr);
#endif
        }
        *xml_ptr += token_length;
        if(!next_token(*xml_ptr, xml_ptr, &token_length))
            return 0;
    }
}

/* Updates the passed official and beta versions, using candidate_ptr as a temp */
static int internal_find_latest_version(const char *xml, download_data_t *official_ptr, 
  download_data_t *beta_ptr, download_data_t *candidate_ptr) {
    int token_length;
    int state;
    int update_id = -1;

    /* initialize data */
    init_download_data(candidate_ptr);
    state = OUTSIDE_MSG_TAG;

    /* Main loop */
    /* Discard first token, as it contains the signature */
    if (!next_token(xml, &xml, &token_length))
        return 0;
    xml += token_length;

    if(!next_token(xml, &xml, &token_length))
        return 0;

    while(xml != NULL) {
#ifdef DEBUG
        char print_buf[1024];
        strncpy(print_buf, xml, token_length);
        print_buf[token_length] = (char) '\0';
        //printf("State: %d  token:%s\n", state, print_buf);
#endif

        if (state == INSIDE_MSG_OPENING_TAG) {
            if (!process_message_tag(&xml, official_ptr, beta_ptr, candidate_ptr, update_id)) return 0;
            state = INSIDE_MSG_TAG_BODY;
        } else if (state == INSIDE_MSG_TAG_BODY) {
            /* Recursive <msg > </msg> tags are illegal */
            if (token_length == 4 && strncmp(xml, "<msg", 4) == 0)
                return 0;
            if (token_length == 6 && strncmp(xml, "</msg>", 6) == 0)
                state = OUTSIDE_MSG_TAG;
        } else if (state == OUTSIDE_MSG_TAG) {
            if (token_length == 4 && strncmp(xml, "<msg", 4) == 0)
                state = INSIDE_MSG_OPENING_TAG;
            else if (strncmp(xml, "id=", 3) == 0) {
                if (update_id != -1) return 0; /* Duplicate update_id attributes -- fail */
                if(!set_int(&update_id, xml)) return 0;
            }
        }
        xml += token_length; 
        if(!next_token(xml, &xml, &token_length)) {
            return 0;
        }
    }

    return 1;
}


/* Updates the passed official and beta versions, using candidate_ptr as a temp */
static int find_latest_version(char *xml, download_data_t *official_ptr, download_data_t *beta_ptr) {
    /* Simple wrapper to simplify memory management */
    download_data_t d_temp;
    int return_code;
 
    /* 4 malloc()s with string termination b/c strncmp may not null terminate */
    d_temp.urn =      (char *) malloc (MAX_ATTRIBUTE_LENGTH + 1); d_temp.urn[MAX_ATTRIBUTE_LENGTH]      = (char) '\0';
    d_temp.url =      (char *) malloc (MAX_ATTRIBUTE_LENGTH + 1); d_temp.url[MAX_ATTRIBUTE_LENGTH]      = (char) '\0';
    d_temp.uname =    (char *) malloc (MAX_ATTRIBUTE_LENGTH + 1); d_temp.uname[MAX_ATTRIBUTE_LENGTH]    = (char) '\0';
    d_temp.icommand = (char *) malloc (MAX_ATTRIBUTE_LENGTH + 1); d_temp.icommand[MAX_ATTRIBUTE_LENGTH] = (char) '\0';
#ifdef DEBUG
	printf("d_temp pointers %p %p %p %p\n", d_temp.urn, d_temp.url, d_temp.uname, d_temp.icommand);
#endif

    init_download_data(&d_temp);

    return_code = internal_find_latest_version(xml, official_ptr, beta_ptr, &d_temp);

#ifdef DEBUG
	printf("d_temp pointers %p %p %p %p\n", d_temp.urn, d_temp.url, d_temp.uname, d_temp.icommand);
#endif

    /* 4 free()s */
    free_if_not_null(d_temp.urn);
    free_if_not_null(d_temp.url);
    free_if_not_null(d_temp.uname);
    free_if_not_null(d_temp.icommand);

    return return_code;
}


int main(int argc, char *argv[]) {
    FILE *xml_file;
    char buf[4096];
    char print_buf[2048];
    char *buf_ptr;
    const char *token_ptr;
    int token_length;
    download_data_t official;
    download_data_t beta;

    init_download_data(&official);
    init_download_data(&beta);

    xml_file = fopen(XML_TEST_FILE, "r");

    if (xml_file == NULL)
        return 1;

    printf("File open\n");

    /* Load xml into buf */
    buf_ptr = buf;
    while (1) {
        int next_byte = fgetc(xml_file);
        if (next_byte == EOF)
            break;
        *buf_ptr = (char) next_byte;
        ++buf_ptr;
    }
    ++buf_ptr;
    *buf_ptr = '\0';
   
    fclose(xml_file);
    printf("File closed\n");

    printf("XML=%s\n", buf);

    /* Tokenize */
    next_token(buf, &token_ptr, &token_length);
    while (token_ptr != NULL) {
        strncpy(print_buf, token_ptr, token_length);
        print_buf[token_length] = (char) '\0';
        //printf("Token : %s\n", print_buf);
        token_ptr += token_length;
        next_token(token_ptr, &token_ptr, &token_length);
    }

    if (!find_latest_version(buf, &official, &beta))
        printf("Parse failed\n");

    write_version(stdout, &official);
    write_version(stdout, &beta);

    return 0;
}
