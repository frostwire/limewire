#!/usr/bin/env python

import launchpad
import sys
import getopt

def handle(response):
    print response.geturl()

def request_template(email, password, group='limewire'):
    browser = launchpad.login(email, password)
    page = browser.open('https://translations.launchpad.net/%s/trunk/+pots/%s/+export' % (group, group))
    handle(page)
    browser.select_form(nr=1)
    page = browser.open(browser.click())
    handle(page)

def main(args):
    try:
        opts, args = getopt.getopt(args, "", ["email=", "password="])
    except getopt.GetoptError:
        exit(2)
    for opt, arg in opts:
        if opt == "--email":
            email = arg
        elif opt == "--password":
            password = arg
    request_template(email, password)

if __name__ == "__main__":
    main(sys.argv[1:])
