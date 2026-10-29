#!/usr/bin/env python

import logging
import sys
import sets
import re

import polib

import documentbuilder
import distances

logging.basicConfig(level=logging.DEBUG,
                    filename='casing.log',
                    filemode='w',
                    format='%(asctime)s %(levelname)s %(message)s')

book_title_exceptions = sets.Set(("for", "in", "with", "to", "and", "or", "but",
                                  "the", "a", "an", "on", 'at', 'of', 'out',
                                  'into'))

multi_word_exceptions = ('P2P Network', 'My Library', 
                         'Digital Audio Access Protocol Service',
                         'Save Folder',
                         'Show Bug',
                         'Try Again',
                         'Advanced -> Files -> iTunes',
                         'Send Bug',
                         '{0}',
                         'My Documents',
                         'LimeWire Store')

proper_names = sets.Set(("LimeWire", 'DHT', 'Ultrapeer', 'Leaf', 'iTunes',
                         'Alpha', 'Friend', 'P2P', 'Desktop', 'I'))


sentence_marks = re.compile('[.!?]')
word_splitter = re.compile('[ ,:&]')
html = re.compile('<[A-z]+>')

def split_words(text):
    for exception in multi_word_exceptions:
        text = text.replace(exception, 'LimeWire')
    words = word_splitter.split(text)
    return [word for word in words if word and word[0].isalpha()]

def is_book_title_case(text):
    words = split_words(text)
    for word in words:
        if word not in book_title_exceptions:
            first = word[0]
            if not first.isupper():
                logging.debug("not title case: %s | %s" % (word, text))
                return False
    return True

def sentences_are_sentence_style_case(sentences):
    sentences = sentence_marks.split(sentences)
    for sentence in sentences:
        if not is_sentence_style_case(sentence):
            return False
    return True

def contains_html(text):
    """
    Returns non-nil if text contains html tags.
    """
    return html.search(text)

def is_sentence_style_case(text):
    words = split_words(text)
    if not words:
        return True
    # first word should be capitalized
    if not is_book_title_case(words[0]):
        return False
    for word in words[1:]:
        if word not in proper_names:
            first = word[0]
            if not first.islower():
                logging.debug("not sentence style: %s | %s" % (word, words))
                return False
    return True

def get_inconsistent_cases(template_name):
    template = polib.pofile(template_name)
    return [entry for entry in template if not contains_html(entry.msgid)
            and not is_book_title_case(entry.msgid)
            and not sentences_are_sentence_style_case(entry.msgid)]

def create_doc_entry(entry, builder):
    doc = ""
    doc += builder.br()
    doc += builder.p(entry.msgid)
    doc += builder.p("Sources:" + builder.br() + distances.create_sources_document(entry.occurrences, builder))
    return doc

def main(template_name):
    irregulars = get_inconsistent_cases(template_name)
    html = ''
    html += "<html><body>"
    html_builder = documentbuilder.HtmlDocumentBuilder()
    html += html_builder.h3("%s entries have inconsistent casing" % len(irregulars))
    for entry in irregulars:
        html += create_doc_entry(entry, html_builder)
    html += "</body></html>"
    print html

if __name__ == '__main__':
    args = sys.argv[1:]
    if len(args) != 1:
        print """
Usage: casing.py template.pot

Prints out all the entries that follow neither book title case nor sentence style case.
"""
    else:
        main(*args)
        
