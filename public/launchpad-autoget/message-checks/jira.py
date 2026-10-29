#!/usr/bin/env python

import logging
import SOAPpy
import sys
import pydoc

# several generic limewire jira soap calls

def login(user_name, password):
    soap = SOAPpy.WSDL.Proxy("https://www.limewire.org/jira/rpc/soap/jirasoapservice-v2?wsdl")
    auth = soap.login(user_name, password)
    return soap, auth

def is_closed(soap, auth, issue_id):
    """
    Returns true if issue is closed.
    """
    issue = soap.getIssue(auth, issue_id)
    return issue.status == '6'

def is_in_code_review(soap, auth, issue_id):
    """
    Returns true if the issue is "In Code Review"
    """
    issue = soap.getIssue(auth, issue_id)
    return issue.status == '10001'

def get_last_time_updated(soap, auth, issue_id):
    """
    Returns the last time the issue was updated as a tuple
    """
    issue = soap.getIssue(auth, issue_id)
    return issue.updated

def get_last_comment(soap, auth, issue_id):
    """
    Returns the last comment in issue.
    """
    comments = soap.getComments(auth, issue_id)
    return comments[-1].body

def add_comment(soap, auth, issue_id, comment):
    """
    Adds a comment to the issue.
    """
    soap.addComment(auth, issue_id, {'body': comment})

def reopen_issue(soap, auth, issue):
    """
    Reopens issue.
    """
    fields = soap.getFieldsForAction(auth, issue.key, '3')
    args = []
    for field in fields:
        if field.id in issue:
            args.append({'id': field.id, 'values': issue[field.id]})
    soap.progressWorkflowAction(auth, issue.key, '3', args)

functions = [is_closed, is_in_code_review, get_last_time_updated, get_last_comment, add_comment, reopen_issue]

if __name__ == "__main__":
    try :
        args = sys.argv[1:]
        # first two args are username and password
        soap, auth = login(*args[:2])
        # third arg is function name, rest are passed to function
        print eval(args[2])(soap, auth, *args[3:])
    except Exception, e:
        print e
        print """
Usage: jira.py username password function_name arguments

Available functions:
        """
        textdoc = pydoc.TextDoc()
        for func in functions:
            print textdoc.document(func)
