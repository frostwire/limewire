class HtmlDocumentBuilder:
    
    mapping = {'removed': 'del'}

    def __getattr__(self, attribute):
        if attribute == 'br':
            def br():
                return "<br/>\n"
            return br
        elif attribute in ("red", "green"):
            def color(text):
                return "<font color='%s'>%s</font>" % (attribute, text)
            return color
        elif attribute in HtmlDocumentBuilder.mapping:
            def mapped(text):
                tag = HtmlDocumentBuilder.mapping[attribute]
                return "<%s>%s</%s>" % (tag, text, tag)
            return mapped
        else:
            def tag(text):
                return "<%s>%s</%s>" % (attribute, text, attribute)
            return tag


class JiraWikiDocumentBuilder:
    
    html_to_wiki = {'b': '*', 'em': '_', 'removed': '-', 'ins': '+' }

    def __getattr__(self, attribute):
        
        if attribute in JiraWikiDocumentBuilder.html_to_wiki:
            def wrap(text):
                tag = JiraWikiDocumentBuilder.html_to_wiki[attribute]
                return "%s%s%s" % (tag, text, tag)
            return wrap
        elif attribute.startswith('h'):
            def header(text):
                return "%s. %s\n" % (attribute, text)
            return header
        elif attribute == 'p':
            def p(text):
                return "%s\n\n" % text
            return p
        elif attribute == 'br':
            def br():
                return "\\\\\n"
            return br
        elif attribute in ('red', 'green'):
            def color(text):
                return "{color:%s}%s{color}" % (attribute, text)
            return color

