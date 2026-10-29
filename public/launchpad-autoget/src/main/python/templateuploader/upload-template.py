#!/usr/bin/python

#
#  This program is free software; you can redistribute it and/or modify
#  it under the terms of the GNU General Public License as published by
#  the Free Software Foundation; either version 2 of the License, or
#  (at your option) any later version.
#
#  This program is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU General Public License for more details.
#
#  You should have received a copy of the GNU General Public License
#  along with this program; if not, write to the Free Software
#  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
#

import launchpad
import sys
import getopt

def upload(email, password, filename):
    """Uploads file with filename to launchpad
    """
    browser = launchpad.login(email, password)
    page = browser.open('https://translations.launchpad.net/limewire/trunk/+pots/limewire/+upload')
    browser.select_form(nr=1)
    browser.form.add_file(open(filename), filename=filename)
    page = browser.open(browser.click())
    print page.geturl()

def usage():
    """Prints the usage of this small program.
    
    """
    print """
    Script to upload a translation template to launchpad.net.
    Provide all of the following arguments:
    
    ./upload-template.py --email user_email --password user_password keys.pot
    """

def main(argv):
    """Parses command argv and starts upload process.
    """
    try:
        opts, args = getopt.getopt(argv, "h", ["help", "email=", "password="])
    except getopt.GetoptError:
        usage()
        exit(2)
    for opt, arg in opts:
        if opt in ("-h", "--help"):
            usage()
            exit(0)
        elif opt == "--email":
            email = arg
        elif opt == "--password":
            password = arg

    upload(email, password, args[0])

        
if __name__ == "__main__":
    main(sys.argv[1:])
