#!/usr/bin/env python
# -*- coding: utf-8 -*-
import logging
import sys
import polib

vowel_map = { 'A': u'Ä', 'E': u'É', 'I': u'Í', 'O': u'Ö', 'U': u'Ü', 'a': u'ä',
              'e': u'é', 'i': u'í', 'o': u'ö', 'u': u'ü' }

def translate_entry(text):
    result = "_"
    for letter in text:
        if letter in vowel_map:
            result += vowel_map[letter]
        else :
            result += letter
    result += "_"
    return result

def translate(template):
    for entry in template:
        if entry.msgid_plural:
            singular = translate_entry(entry.msgid)
            logging.debug("%s => %s" % (entry.msgid, singular))
            plural = translate_entry(entry.msgid_plural)
            logging.debug("\t%s => %s" % (entry.msgid_plural, plural))
            entry.msgstr_plural = { '0': singular, '1': plural }
        else:
            translation = translate_entry(entry.msgid)
            logging.debug("%s => %s" % (entry.msgid, translation))
            entry.msgstr = translation

    logging.debug("Saving ul.po")
    template.save("ul.po")

def main(template_file):
    translate(polib.pofile(template_file))

if __name__ == "__main__":
    args = sys.argv[1:]
    if len(args) != 1:
        print """
Usage: umlaut.py template_po_file
"""
    else:
        main(*args)
