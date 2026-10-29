#!/usr/bin/env python

import logging
import sys
import polib
import distances
import documentbuilder

from diff_match_patch import diff_match_patch


def compare_templates(old_template, new_template):
    """
    Returns list of added, changed and removed entries

    changed are triple of old, new entry and diff
    """
    old_entries_by_msgid = dict()
    old_entries_by_occurences = dict()
    for entry in old_template:
        old_entries_by_msgid[entry.msgid] = entry
        old_entries_by_occurences[str(entry.occurrences)] = entry

    added = []
    changed = []
    for entry in new_template:
        if str(entry.occurrences) in old_entries_by_occurences:
            if entry.msgid not in old_entries_by_msgid:
                changed.append((old_entries_by_occurences[str(entry.occurrences)], entry))
            else:
                del(old_entries_by_msgid[entry.msgid])
        elif entry.msgid not in old_entries_by_msgid:
            added.append(entry)
        else:
            del old_entries_by_msgid[entry.msgid]

    removed = old_entries_by_msgid.values()
    return added, distances.annotate_with_diffs([changed])[0], removed


def compare_files(old_template_file, new_template_file):
    return compare_templates(polib.pofile(old_template_file),
                             polib.pofile(new_template_file))

def create_doc_entry(entry, added, builder):
    doc = ""
    doc += builder.br()
    if added:
        doc += builder.p(builder.green(entry.msgid))
    else:
        doc += builder.p(builder.red(entry.msgid))
    doc += builder.p("Sources:" + builder.br() + distances.create_sources_document(entry.occurrences, builder))
    return doc

def create_document(added, changed, removed, builder):
    doc = builder.h3("%s entries were added:" % len(added))
    for entry in added:
        doc += create_doc_entry(entry, True, builder)

    doc += builder.br() + builder.h3("%s entries were changed:" % len(changed))
    doc += distances.create_document(changed, builder)
    doc += builder.br() + builder.h3("%s entries were removed:" % len(removed))
    for entry in removed:
        doc += create_doc_entry(entry, False, builder)
    return doc


def main(old_template_file, new_template_file):
    added, changed, removed = compare_files(old_template_file, new_template_file)
    html = "<html><body>"
    html += create_document(added, changed, removed, documentbuilder.HtmlDocumentBuilder())
    html += "</body></html>"
    print html

if __name__ == "__main__":
    args = sys.argv[1:]
    if len(args) != 2:
        print """
Usage: compare.py old_template_file new_template_file
"""
    else:
        main(*args)
