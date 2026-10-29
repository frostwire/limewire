
#ifndef __XML_PARSER__
#define __XML_PARSER__

#ifndef __INTERFACES__
#include "interfaces.h"
#endif

//*********************************************************

struct UpdateData
{
	UpdateData();
	UpdateData( const UpdateData & data );
	~UpdateData();

	void Reset( bool bAllocate );

	const char * getDownloadURN( void )			{ return urn;		}
	const char * getDownloadBackupURL( void )	{ return url;		}
	const char * getDownloadFilename( void )	{ return uname; 	}
	const char * getDownloadICommand( void )	{ return icommand;	}

	bool isFree( void );

	bool operator==( const UpdateData & rhs ) const;
	bool operator<(  const UpdateData & rhs ) const;
	bool operator>(  const UpdateData & rhs ) const;
	UpdateData & operator=(
					 const UpdateData & rhs );

private:
    int 					update_id;
    int 					size;

    int 					os_match;
    int 					style;
    int 					free;
    int 					pro;
    int 					version[4]; // major, minor, service, revision
    char				*	urn;
    char				*	url;
    char				*	uname;
    char				*	icommand;

	friend struct Parser;
};

//---------------------------------------------------------

struct Parser : public XmlParser
{
	Parser();

public:
	//	Overrides from XmlParser interface
	virtual void parseUpdateResponse( 
		const char		*	src, 
		size_t				len );

	virtual bool receivedEnoughUpdates( void );

	//	These are expected to return a pointer to a string which does NOT
	//		need to be freed.
	virtual const char * getReleaseBitprint( void );
	virtual const char * getReleaseFileName( void );
	virtual const char * getReleaseICommand( void );

	virtual const char * getBetaBitprint( void );
	virtual const char * getBetaFileName( void );
	virtual const char * getBetaICommand( void );
private:
	bool parseInternal( 
		const char		*	src, 
		size_t				len );

	bool next_token(
		const char		*	xml, 
		const char		**	token, 
		int				*	token_length_ptr);

	void skipWhitespace(
		const char		**	xml );

	bool find_latest_version(
		const char		*	xml, 
		UpdateData		*	official_ptr, 
		UpdateData		*	beta_ptr );

	//	These setter functions expect a pointer to a string that includes the
	//		name of the value being set....  For example, "foo=5" would be a valid
	//		string to pass to the set_int() function
private:
	bool set_int(
		int				*	i_ptr, 
		const char		*	attribute );

	bool set_string(
		char			**	destination_ptr, 
		const char		*	attribute, 
		int					attribute_length );

	bool set_version_number(
		UpdateData		*	d_ptr, 
		const char		*	version_attr, 
		int					version_attr_length );

	//	These setter functions delegate to the above functions, 
	//		if the attribute name is equal to the name parameter
private:
	bool set_int_if_name(
		int				*	i_ptr, 
		const char		*	attribute, 
		const char		*	name ); 

	bool set_string_if_name(
		char			**	destination, 
		const char		*	attribute, 
		int					attribute_length, 
		const char		*	name );

	bool set_version_if_name(
		UpdateData		*	d_ptr, 
		const char		*	attribute, 
		int					attribute_length, 
		const char		*	name );

	//	stupid function which checks versions and copies the struct if needed
private:
	void update_latest_version(
		UpdateData		*	current, 
		const UpdateData*	candidate );

	//	parsing loop & work function
private:
	bool internal_find_latest_version(
		const char		*	xml, 
		UpdateData		*	official_ptr, 
		UpdateData		*	beta_ptr, 
		UpdateData		*	candidate_ptr );

	bool process_message_tag(
		const char		**	xml_ptr, 
		UpdateData		*	official_ptr,
		UpdateData		*	beta_ptr, 
		UpdateData		*	candidate_ptr, 
		int					update_id );

private:
	int						m_nNumUpdates;
	UpdateData				m_updateBeta;
	UpdateData				m_updateRelease;
	UpdateData				m_updateTemp;
};

//*********************************************************

#endif










