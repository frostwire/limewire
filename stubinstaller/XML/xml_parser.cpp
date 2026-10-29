#include <stdlib.h>
#include <stdio.h>
#include <string.h>

#ifndef __XML_PARSER__
#include "xml_parser.h"
#endif

//****************************************
#define XML_TEST_FILE "version.xml"
#define MIN_OFFICIAL_STYLE 1			//	This is used to determine BETA/RELEASE
#define NO_MAIN
//----------------------------------------
#ifndef NO_MAIN
	#ifdef NDEBUG
	#define MIN_UPDATE_MESSAGES	3
	#else
	#define MIN_UPDATE_MESSAGES 1
	#endif
#else
	#define MIN_UPDATE_MESSAGES	3
#endif
//****************************************

// Constants used in the special-case parsing of <![CDATA[Arbitrary String Here]]>
#define CDATA_TAG "<![CDATA["
#define CDATA_TAG_LENGTH 9

// 8 kb minus one byte
#define MAX_ATTRIBUTE_LENGTH 8191

#define OS_NAME "windows"		//	MUST BE LOWER CASE TO WORK!!!   -dn

//****************************************

/* States of our xml parser state machine */
enum {
    OUTSIDE_MSG_TAG,
    INSIDE_MSG_OPENING_TAG,
    INSIDE_MSG_TAG_BODY
};

//----------------------------------------

UpdateData::UpdateData()
{
	urn=0;
	url=0;
	uname=0;
	icommand=0;

	Reset(true);
}

//----------------------------------------

UpdateData::UpdateData( const UpdateData & rhs )
{
	(*this) = rhs;	//	Strings are freed & (re)allocated inside this call
}

//----------------------------------------

UpdateData::~UpdateData()
{
	Reset(false);	//	Strings are freed inside this call
}

//----------------------------------------

void UpdateData::Reset( bool bAllocate )
{
    update_id = -1;
    size = 0;

    os_match = 1; /* The os matches by default. */
    style    = -1; /* -1 indicates an error that no style was specified */
    free     = 0;
    pro      = 0;
    memset( version,0,sizeof(version) );

	if( urn )
	{
		delete urn;
		urn=0;
	}

	if( url )
	{
		delete url;
		url=0;
	}

	if( uname )
	{
		delete uname;
		uname=0;
	}

	if( icommand )
	{
		delete icommand;
		icommand=0;
	}

	if( bAllocate )
	{
		urn		=	new char [ MAX_ATTRIBUTE_LENGTH ];	urn[0]=0;
		url		=	new char [ MAX_ATTRIBUTE_LENGTH ];	url[0]=0;
		uname	=	new char [ MAX_ATTRIBUTE_LENGTH ];	uname[0]=0;
		icommand=	new char [ MAX_ATTRIBUTE_LENGTH ];	icommand[0]=0;
	}
}

//****************************************

bool UpdateData::operator==( const UpdateData & rhs ) const
{
	if(  version[0] == rhs.version[0]
	  && version[1] == rhs.version[1]
	  && version[2] == rhs.version[2]
	  && version[3] == rhs.version[3] )
		  return true;

	return false;
}

//----------------------------------------

bool UpdateData::operator<( const UpdateData & rhs ) const
{
	if(  version[0]  < rhs.version[0] )
		return true;

	if(  version[0] == rhs.version[0]
	  && version[1]  < rhs.version[1] )
		return true;

	if(  version[0] == rhs.version[0]
	  && version[1] == rhs.version[1]
	  && version[2]  < rhs.version[2] )
		return true;

	if(  version[0] == rhs.version[0]
	  && version[1] == rhs.version[1]
	  && version[2] == rhs.version[2]
	  && version[3]  < rhs.version[3] )
		return true;

	return false;
}

//----------------------------------------

bool UpdateData::operator>( const UpdateData & rhs ) const
{
	if(  version[0]  > rhs.version[0] )
		return true;

	if(  version[0] == rhs.version[0]
	  && version[1]  > rhs.version[1] )
		return true;

	if(  version[0] == rhs.version[0]
	  && version[1] == rhs.version[1]
	  && version[2]  > rhs.version[2] )
		return true;

	if(  version[0] == rhs.version[0]
	  && version[1] == rhs.version[1]
	  && version[2] == rhs.version[2]
	  && version[3]  > rhs.version[3] )
		return true;

	return false;
}

//----------------------------------------

UpdateData & UpdateData::operator=( const UpdateData & rhs )
{
	Reset(false);	//	Strings are freed inside this call

	//	Raw member copy
	memcpy( this, &rhs, sizeof(rhs) );

	//	Now duplicate strings - note that the memcpy simply COPIED the pointers, 
	//		so we can safely call strlen() on them to determine the size of the
	//		buffer we need to allocate.  Once allocated, we copy the string and 
	//		update the pointers to point to the newly allocated & copied buffers.
	if( urn )
	{
		size_t len = strlen( urn );
		char * ptr = new char[len+1];
		strcpy( ptr, urn );
		urn=ptr;
	}

	if( url )
	{
		size_t len = strlen( url );
		char * ptr = new char[len+1];
		strcpy( ptr, url );
		url=ptr;
	}

	if( uname )
	{
		size_t len = strlen( uname );
		char * ptr = new char[len+1];
		strcpy( ptr, uname );
		uname=ptr;
	}

	if( icommand )
	{
		size_t len = strlen( icommand );
		char * ptr = new char[len+1];
		strcpy( ptr, icommand );
		icommand=ptr;
	}

	return (*this);
}

//****************************************

Parser::Parser()
{
	m_nNumUpdates=0;
}

//****************************************

void Parser::parseUpdateResponse( const char * src, size_t len )
{
	bool bSuccess=find_latest_version( src, &m_updateRelease, &m_updateBeta );

	if( bSuccess )
		m_nNumUpdates++;
}

//----------------------------------------

bool Parser::receivedEnoughUpdates( void )
{
	return m_nNumUpdates >= MIN_UPDATE_MESSAGES;
}

//----------------------------------------

const char * Parser::getReleaseBitprint( void )
{
	const char * ptr = 0;

	if( receivedEnoughUpdates() )
		ptr=m_updateRelease.getDownloadURN();

	return ptr;
}

//----------------------------------------

const char * Parser::getReleaseFileName( void )
{
	const char * ptr = 0;

	if( receivedEnoughUpdates() )
		ptr=m_updateRelease.getDownloadFilename();

	return ptr;
}

//----------------------------------------

const char * Parser::getReleaseICommand( void )
{
	const char * ptr = 0;
	
	if( receivedEnoughUpdates() )
		ptr=m_updateRelease.getDownloadICommand();

	return ptr;
}

//****************************************

const char * Parser::getBetaBitprint( void )
{
	const char * ptr = 0;

	if( receivedEnoughUpdates() )
		ptr=m_updateBeta.getDownloadURN();

	return ptr;
}

//----------------------------------------

const char * Parser::getBetaFileName( void )
{
	const char * ptr = 0;

	if( receivedEnoughUpdates() )
		ptr=m_updateBeta.getDownloadFilename();

	return ptr;
}

//----------------------------------------

const char * Parser::getBetaICommand( void )
{
	const char * ptr = 0;
	
	if( receivedEnoughUpdates() )
		ptr=m_updateBeta.getDownloadICommand();

	return ptr;
}

//****************************************

void Parser::skipWhitespace(
	const char		**	xml )
{
	*xml += strspn(*xml, " \t\n\r");
}

//****************************************

// Gets the next token in the xml string, returning a pointer to its start,
//  and using a passed int pointer to also return the length of the token.
//  This does not allocate any new memory nor does it modify the xml string 
//	
//	Returns true if a valid token appears to have been found
bool Parser::next_token(
	const char		*	xml, 
	const char		**	token, 
	int				*	token_length_ptr ) 
{
    bool in_double_quotes;
    bool in_single_quotes;

    //	First of all, move xml to the next non-whitespace character
    xml += strspn(xml, " \t\n\r");

	//	Reached end of input?
    if( xml[0] == '\0')  
	{
        *token_length_ptr = -1;
        *token = NULL;
        return false;
    }

    // Special case for <![CDATA[ tags
    if( strncmp(CDATA_TAG, xml, CDATA_TAG_LENGTH) == 0) 
	{
        char *end_tag = strstr(xml, "]]>");

        if( end_tag == NULL) 
		{
			//	Couldn't find the close of the CDATA section
            *token_length_ptr = -1;
            *token = NULL;
            return false;
        } 

		//	Found the close of CDATA
        *token_length_ptr = end_tag - xml + 3;
        *token = xml;
        return true;
    }

    // Scan for breaking whitespace OR an escaped character OR 
    // a double-quote character 
    in_double_quotes = false;
    in_single_quotes = false;
    *token_length_ptr = 1;

    while(true) 
	{
        if( in_double_quotes) 
            *token_length_ptr += strcspn(&xml[*token_length_ptr], "\"\\");
		else if( in_single_quotes) 
            *token_length_ptr += strcspn(&xml[*token_length_ptr], "\'\\");
		else 
            *token_length_ptr += strcspn(&xml[*token_length_ptr], "<> \t\n\r\"\'");

        switch(xml[*token_length_ptr]) 
		{ 
			case '\0':
                // Don't run off the end of the string
                *token = xml;
                return true;				break;

            case '<':
                // New tag inside 
                *token = xml;
                return true;				break;

            case '>':
                // Close current tag
                ++(*token_length_ptr);	//	include the '>'
                *token = xml;
                return true;				break;

            case '\\':
				// Skip escaped characters
                *token_length_ptr += 2;     break;

            case '\"':
				// Deal with double quotes
                ++(*token_length_ptr);

				//	NOTE:  Don't care about double quotes if we are 
				//			currently in a single quote already  --DN
				if( in_single_quotes )
					assert(0);

				if( in_double_quotes) 
				{
					//	closing quotes
					*token = xml;
					return true;
				} 

				//	opening double quote
				in_double_quotes = true;	break;

            case '\'':
				// Deal with single quotes 
                ++(*token_length_ptr);

				//	NOTE:  Don't care about single quotes if we are 
				//			currently in a double quote already  --DN
				if( in_double_quotes )
					assert(0);

				if( in_single_quotes) 
				{
					*token = xml;
					return true;
				}

				//	opening single quote
				in_single_quotes = true;	break;

			default:
                *token = xml;
                return true;				break;
        }
    }
}

//----------------------------------------
// Updates the passed official and beta versions

bool Parser::find_latest_version(
	const char		*	xml, 
	UpdateData		*	official_ptr, 
	UpdateData		*	beta_ptr )
{
    bool bSuccess=false;
 
	UpdateData temp;	//	Allocates
    bSuccess = internal_find_latest_version(
		 xml, 
		 official_ptr, 
		 beta_ptr, 
		&temp );
	temp.Reset( false );//	Frees

    return bSuccess;
}

//****************************************
// Extracts an int from an attribute

bool Parser::set_int(
	int				*	i_ptr, 
	const char		*	attribute ) 
{
    char *start;

    start = strchr(attribute, '=');
    if( start == NULL)
        return false;	//	didn't find the '='

	// Skip over single or double quotes
    start += 2;
    if( start[-1] != '\'' && start[-1] != '\"')
        return false;

    *i_ptr = atoi(start);

    return true;
}

//----------------------------------------
// Extracts a string from an attribute.
// Fails if *destination is NULL, or if attribute is not a well-formed xml attribute.

bool Parser::set_string(
	char			**	destination_ptr, 
	const char		*	attribute, 
	int					attribute_length ) 
{
    const char *last_char_ptr;
    int string_length;

    if( *destination_ptr == NULL)
        return false;

    if(attribute_length > MAX_ATTRIBUTE_LENGTH) 
	{
		//	BAD!  realloc only works with malloc() not new
//        if( realloc(*destination_ptr, attribute_length + 1) == NULL)
		if( *destination_ptr )
			delete *destination_ptr;

		*destination_ptr = new char[attribute_length + 1];

        (*destination_ptr)[attribute_length] = (char) '\0';
		(*destination_ptr)[0] = (char) '\0';
    }

    last_char_ptr = attribute + attribute_length - 1;

    // Advance attribute pointer to after equal sign and opening quote
    attribute = (const char *)memchr(attribute, '=', attribute_length);
    if( attribute == NULL)
        return false;
     attribute+=2;
    
    // make sure string begins and ends with quotes 
    if( attribute[-1] != '\"' && attribute[-1] != '\'')
        return false;
    if( attribute[-1] != *last_char_ptr)
        return false;

    // Don't include the closing quote in string_length.           
    // Othertwise string_length would be (last_char_ptr-attribute+1)
    string_length = (int) (last_char_ptr-attribute);

    strncpy(*destination_ptr, attribute, string_length);
    (*destination_ptr)[string_length] = (char) '\0';

    return true;
}

//----------------------------------------
// Sets the version number on the passed version_t

bool Parser::set_version_number(
	UpdateData		*	d_ptr, 
	const char		*	version_attr, 
	int					version_attr_length ) 
{
    int i;
    const char *last_char_ptr;

    if( strncmp("for=",version_attr,4) != 0 )
        return false;	

    last_char_ptr = version_attr + version_attr_length - 1;

    // Check that it begins and ends with quotes
    if( version_attr[4] != '\"' && version_attr[4] != '\'' )
        return false;
    if( version_attr[4] != *last_char_ptr )
        return false;

    // skip to after the quote
    version_attr += 5;

    // Extract up to 4 numbers from the version string
    for( i=0; i<4; ++i ) 
	{
        d_ptr->version[i] = atoi(version_attr); 
        version_attr += strspn(version_attr, "0123456789");
        version_attr += strcspn(version_attr, "01234566789 \t\"");
        if( version_attr == last_char_ptr)
            break;
    }

    // Check for expected character after quotes
    switch( version_attr[1] ) 
	{
        case ' ':
        case '>':
        case '\t':   
        case '\r':
        case '\n':
            return true;
    }

    return false;
}

//****************************************
// Sets an int, if the beginning of the attribute matches name
// A mis-match of name and the attribute name is not considered a failure

bool Parser::set_int_if_name(
	int				*	i_ptr, 
	const char		*	attribute, 
	const char		*	name ) 
{
    // Return success early if there is a name mis-match
    if( strncmp(attribute, name, strlen(name)) != 0 )
        return true;	//	wrong name
    return set_int(i_ptr, attribute);
}

//----------------------------------------
// Sets a string, if the beginning of the attribute matches name 
// A mis-match of name and the attribute name is not considered a failure 

bool Parser::set_string_if_name(
	char			**	destination, 
	const char		*	attribute, 
	int					attribute_length, 
	const char		*	name ) 
{
    // Return success early if there is a name mis-match
    if( strncmp(attribute, name, strlen(name)) != 0 )
        return true;	//	wrong name
    return set_string(destination, attribute, attribute_length);
}

//----------------------------------------
// Sets a version number, if the beginning of the attribute matches name 
// A mis-match of name and the attribute name is not considered a failure

bool Parser::set_version_if_name(
	UpdateData		*	d_ptr, 
	const char		*	attribute, 
	int					attribute_length, 
	const char		*	name ) 
{
    if( strncmp(attribute, name, strlen(name)) != 0 )
        return true;	//	wrong name
    return set_version_number(d_ptr, attribute, attribute_length);
}

//****************************************
// If candidate is newer than current, this function replaces current with candidate 
// and nulls out the pointers in candidate                                           

void Parser::update_latest_version(
	UpdateData		*	current, 
	const UpdateData*	candidate ) 
{
	if( (*candidate) < (*current) )
       return;

	(*current) = (*candidate);	//	Allocates new pointers
}

//****************************************
// Updates the passed official and beta versions, using candidate_ptr as a temp

bool Parser::internal_find_latest_version(
	const char		*	xml, 
	UpdateData		*	official_ptr, 
	UpdateData		*	beta_ptr, 
	UpdateData		*	candidate_ptr ) 
{
    int token_length;
    int state;
    int update_id = -1;

    // initialize data
	candidate_ptr->Reset(true);
    state = OUTSIDE_MSG_TAG;

    // Discard first token, as it contains the signature
    if( !next_token(xml, &xml, &token_length) )
        return false;

    xml += token_length;

    if( !next_token(xml, &xml, &token_length) )
        return false;

    // Main loop
    while(xml != NULL) 
	{
        if( state == INSIDE_MSG_OPENING_TAG ) 
		{
            if( !process_message_tag(&xml, official_ptr, beta_ptr, candidate_ptr, update_id)) 
				return false;
            state = INSIDE_MSG_TAG_BODY;

			//	BUGFIX: token_length holds the last length of the token preceeding the <msg> block
			token_length=1;		//	to eat the closing '>' from the '</msg>' tag
        }
		else if( state == INSIDE_MSG_TAG_BODY ) 
		{
            // Recursive <msg > </msg> tags are illegal
            if( token_length == 4 && strncmp(xml,"<msg",4) == 0 )
                return false;

            if( token_length == 6 && strncmp(xml,"</msg>",6) == 0 )
                state = OUTSIDE_MSG_TAG;
        } 
		else if( state == OUTSIDE_MSG_TAG ) 
		{
            if( token_length == 4 && strncmp(xml,"<msg",4) == 0 )
                state = INSIDE_MSG_OPENING_TAG;
            else if( strncmp(xml,"id=",3) == 0 ) 
			{
                if( update_id != -1 ) 
					return false; // Duplicate update_id attributes -- fail

                if( !set_int(&update_id, xml) ) 
					return false;
            }
        }

        xml += token_length; 
        
		//	BUGFIX: this routine would STILL fail if the update message did not end with
		//		a closing tag, ie "</update>\r\n" would fail since next_token would barf
		//		without a token to follow.  Therefore, we skip all whitespace BEFORE hitting
		//		next_token, so the above case would be caught by the next line.
		skipWhitespace( &xml );

		//	BUGFIX: this function would always return false, since next_token sets the
		//		xml pointer to NULL but returns false, causing this function to also
		//		return false;
		if( *xml == 0 )	//	end of XML message
		{
			assert( state == OUTSIDE_MSG_TAG );
			break;
		}

		if( !next_token(xml, &xml, &token_length) ) 
            return false;
    }

    return true;
}

//----------------------------------------
//	subfunction to parse while inside the <msg> </msg> tags

bool Parser::process_message_tag(
	const char		**	xml_ptr, 
	UpdateData		*	official_ptr,
	UpdateData		*	beta_ptr, 
	UpdateData		*	candidate_ptr, 
	int					update_id )
{
    int token_length;

    // clear data
	candidate_ptr->Reset(true);
    candidate_ptr->update_id = update_id;

    // Get token length again
    if( !next_token(*xml_ptr, xml_ptr, &token_length) )
        return false;

    // loop over tokens
    while(*xml_ptr != NULL) 
	{
        if(   token_length == 1 
		  && (*xml_ptr)[0] == '>' ) 
		{
            // Close of msg tag 
            // Sanity checks 
            if( candidate_ptr->os_match==0 )
                return true; // wrong os... skip to next <msg tag 
            if( candidate_ptr->style == -1 ) 
                return true; // No style read... skip to next <msg tag 
            if( candidate_ptr->urn[0] == '\0' )
                return true; // No urn... skip to next <msg tag 

            if( (!candidate_ptr->free) 
			  &&  candidate_ptr->pro )
                return true; // Pro-only update message 
 
            // See if we should update either version
            if( candidate_ptr->style >= MIN_OFFICIAL_STYLE) 
                update_latest_version(official_ptr, candidate_ptr);
            else 
                update_latest_version(beta_ptr, candidate_ptr);

            // successful completion of parsing <msg > tag
            return true;
        } 
		else if( 
			 token_length == 4 
		  && strncmp(*xml_ptr, "<msg", 4) == 0 ) 
		{
            // Illegal recursion of <msg > tags
			assert(0);
            return false;
        } 
		else if( strncmp(*xml_ptr, "os=", 3) == 0 ) 
		{
            // check for proper quotes
            if(  (*xml_ptr)[3] != '\"' 
			  && (*xml_ptr)[3] != '\'' ) 
				return false;

            //if( (*xml_ptr)[3] != (*xml_ptr)[token_length-1])    return 0;
            // os="" is 5 characters long
	
			//	We needed a better test for windows here, since "windows" could appear 
			//		in a list without being the first element
			if( token_length>=(5+strlen(OS_NAME)) )
			{
				char * szBuf=new char[token_length+1];
				strncpy( szBuf, *xml_ptr, token_length );
				szBuf[token_length]=0;
				strlwr( szBuf );

				if( strstr(szBuf+4,OS_NAME)!=NULL )
					candidate_ptr->os_match = 2; // Better than update for all oses
				else
					candidate_ptr->os_match = 0; // False match for os name
				delete [] szBuf;
			}
            else
                candidate_ptr->os_match = 0; // False match for os name 
        } 
		else 
		{
            // Laundry list of attributes to set
            if( !set_int_if_name(&candidate_ptr->size, *xml_ptr, "size=") )
            	return false;
            if( !set_int_if_name(&candidate_ptr->style, *xml_ptr, "style=") )
            	return false;
            if( !set_int_if_name(&candidate_ptr->free, *xml_ptr, "free=") )
            	return false;
            if( !set_int_if_name(&candidate_ptr->pro, *xml_ptr, "pro=") )
            	return false;
            if( !set_string_if_name(&candidate_ptr->urn, *xml_ptr, token_length, "urn=") )
            	return false;
            if( !set_string_if_name(&candidate_ptr->url, *xml_ptr, token_length, "url=") )
            	return false;
            if( !set_string_if_name(&candidate_ptr->uname, *xml_ptr, token_length, "uname=") )
            	return false;
            if( !set_string_if_name(&candidate_ptr->icommand, *xml_ptr, token_length, "icommand=") )
            	return false;
            if( !set_version_if_name(candidate_ptr, *xml_ptr, token_length, "for=") )
            	return false;
        }

        *xml_ptr += token_length;
        if(!next_token(*xml_ptr, xml_ptr, &token_length))
            return false;
    }

	return false;
}

//****************************************
#ifndef NO_MAIN
#if 1

#include <direct.h>
#include <conio.h>
int main(int argc, char *argv[]) 
{
    FILE		*	xml_file;
    char			buf[4096];
	int				bufLen;
    char		*	buf_ptr;
	UpdateData		official;
    UpdateData		beta;
	Parser			parser;

    xml_file = fopen(XML_TEST_FILE, "r");

    if( xml_file == NULL)
        return 1;

	bufLen=0;

    // Load xml into buf
    buf_ptr = buf;
    while (true) 
	{
        int next_byte = fgetc(xml_file);
        if( next_byte == EOF )
            break;
        *buf_ptr = (char) next_byte;
        ++buf_ptr;
		bufLen++;
    }

//    ++buf_ptr;	//	BUGFIX: uninitialized byte after the msg.  buf_ptr was already advanced above
	*buf_ptr = '\0';
   
    fclose(xml_file);

    // Tokenize
	parser.parseUpdateResponse( buf, bufLen );

	printf( "\nRelease: %s - %s\n",
		parser.getReleaseFileName(),
		parser.getReleaseBitprint() );

	printf( "Beta: %s - %s\n",
		parser.getBetaFileName(),
		parser.getBetaBitprint() );


	printf( "\nHit any key to exit\n" );
	while( !kbhit() )
		;		//	Don't clear screen without user input

    return 0;
}
#else

//----------------------------------------
void main(void){}
//----------------------------------------
#endif
#endif
//----------------------------------------
//----------------------------------------





