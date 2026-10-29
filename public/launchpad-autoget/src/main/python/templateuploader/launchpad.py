import mechanize

def handle(response):
    print response.geturl()

def login(email, password):
    '''Returns a mechanize browser that is logged in to launchpad'''
    browser = mechanize.Browser()
    browser.set_handle_robots(False)
    browser.set_handle_referer(True)
    page = browser.open('https://launchpad.net/')
    handle(page)
    link = browser.find_link(text='Log in / Register')
    page = browser.follow_link (link)
    handle(page)
    form = browser.forms().next()                     
    page = browser.open(form.click())
    handle(page)
    form = browser.forms().next()
    form['email'] = email
    form['password'] = password
    page = browser.open(form.click())
    handle(page)
    # confirm page
    form = browser.forms().next()
    page = browser.open(form.click())
    handle(page)
    return browser
