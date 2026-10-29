#!/usr/bin/env python

import distances
import compare
import sys
import logging
import SOAPpy
import jira
import documentbuilder

# Creates an html report of inconsistent messages and appends the report
# to a given issue and the issue is reopened, if it's not open

def create_document(old_template_file, new_template_file, builder):
    changes = compare.compare_files(old_template_file, new_template_file)
    inconsistencies = distances.create_list_of_inconsistencies(new_template_file)
    text = ''
    text += builder.h2('Changes:')
    text += compare.create_document(changes[0], changes[1], changes[2], builder)
    text += builder.br() + builder.h2('Inconsitencies:')
    for i in xrange(1, len(inconsistencies)):
        text  += distances.create_document(inconsistencies[i], builder)
    return text


def main(old_template_file, new_template_file, user_name, password, issue_id):
    wiki = create_document(old_template_file, new_template_file,
                           documentbuilder.JiraWikiDocumentBuilder())
    soap, auth = jira.login(user_name, password)
    last_comment = jira.get_last_comment(soap, auth, issue_id)
    issue = soap.getIssue(auth, issue_id)
    
    soap.addComment(auth, issue_id, {'body': wiki })
    if issue.status != '1':
        jira.reopen_issue(soap, auth, issue)

if __name__ == "__main__":
    args = sys.argv[1:]
    if len(args) != 5:
        print """
        Usage: openissue.py old_template.pot new_template.pot jira-username jira-password jira-issue
        """
    else:
        main(*args)
        
