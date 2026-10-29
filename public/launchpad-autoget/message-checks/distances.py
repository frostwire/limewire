#!/usr/bin/env python
import polib
import editdist
from diff_match_patch import diff_match_patch
import sets
from email.mime.multipart import MIMEMultipart
from email.mime.text import MIMEText
import smtplib
import sys
import logging
import documentbuilder

#logging.basicConfig(level=logging.DEBUG,
#                    filename='distances.log',
#                    filemode='w',
#                    format='%(asctime)s %(levelname)s %(message)s')

false_positives = sets.Set((
    (1, 's'), # purals
    (-1, 's'),
    (1, '&'), # mnemonics
    (-1, '&'),
    (1, ':'), # colon
    (-1, ':')
    ))

def get_distance_matrix(template):
    """
    Computes the edit distances between all entries in the template and
    returns a dictionary with keys template entries and values dictionaries
    with key template entry and value edit distance.
    """
    distance_matrix = dict()
    for i in xrange(0, len(template)):
        entry1 = template[i]
        distance_matrix[entry1] = dict()
        for j in xrange(i+1, len(template)):
            entry2 = template[j]
            distance = editdist.distance(entry1.msgid, entry2.msgid)
            distance_matrix[entry1][entry2] = distance
    return distance_matrix


def get_close_tuples(matrix, max_distance):
    """
    Returns a list of lists of tuples of template entries. Index into the
    first list represents edit distance. Inner lists contain tuples that have
    said edit distance. Lists for edit distances 0 to max_distance are being
    computed.
    """
    close_tuples = []
    for i in xrange(0, max_distance + 1):
        close_tuples.append([])
    
    for entry1, others in matrix.iteritems():
        for entry2, distance in others.iteritems():
            if distance <= max_distance:
                close_tuples[distance].append((entry1, entry2))
    return close_tuples

def print_tuples(tuples):
    for entry in tuples:
        print entry[0]
        print entry[1]
        print ""
        print ""

def annotate_with_diffs(lists):
    """
    Extends tuples in inner lists with the diff between them to a triple.
    """
    differer =  diff_match_patch()
    return [[(one, two, differer.diff_main(one.msgid, two.msgid)) for (one, two) in tuples] for tuples in lists]

def filter_out(lists, exclusions):
    def all_diffs_are_case_related(diffs):
        for op, change in diffs:
            if op != 0 and len(change) == 1:
                if not (-op, change.swapcase()) in diffs:
                    return False
        return True

    def text_too_short_for_changes(triple):
        # ignore small messages
        if len(triple[0].msgid) <= 2:
            return True
        
    def marked_with_casing(triple):
        return "case" in triple[0].comment or "case" in triple[1].comment

    def all_diffs_are_false_positives(diffs):
        for diff in diffs:
            # only look at insert/remove diffs
            if diff[0] != 0 and diff not in exclusions:
                return False
        return True

    def changes_too_big(triple):
        diffs = triple[2]
        for diff in diffs:
            # ignore two character changes
            if diff[0] != 0 and len(diff[1]) > 1:
                return True
        # more changes than length of shorter word
        min_length = min(len(triple[0].msgid), len(triple[1].msgid))
        if len(diffs) >= min_length:
            return True

    def is_excluded(triple):
        diffs = triple[2]
        if text_too_short_for_changes(triple):
            logging.debug("text too short %s" % diffs)
            return True
        if all_diffs_are_case_related(diffs) and marked_with_casing(triple):
            logging.debug("case %s" % diffs)
            return True
        if all_diffs_are_false_positives(diffs):
            logging.debug("false positives %s" % diffs)
            return True
        if changes_too_big(triple):
            logging.debug("changes too big %s" % diffs)
            return True
        
        return False

    return [[triple for triple in triples if not is_excluded(triple)] for triples in lists]

def create_sources_document(sources, builder):
    doc = ""
    for source in sources:
        doc += "%s:%s" % source
        doc += builder.br()
    return doc


def create_document(triples, builder):
    """
    Creates DOCUMENT for flat list of triples.
    """
    def create_diff(diff):
        doc = ""
        for part in diff:
            if part[0] == 0:
                doc += part[1]
            elif part[0] == 1:
                doc += builder.green(part[1])
            else:
                doc += builder.red(part[1])
        return doc

    
    text = ""
    for (one, two, diff) in triples:
        text += builder.br() + builder.p(one.msgid + builder.br() + "vs." + builder.br() + two.msgid)
        text += builder.p("As diff: " + create_diff(diff))
        text += builder.p("Sources:" + builder.br() + create_sources_document(sets.Set(one.occurrences + two.occurrences), builder))
    return text

def create_email(sender, to, html):
    """
    Creates HTML email.
    """
    msg = MIMEMultipart("alternative")
    msg["Subject"] = "Message inconcistencies"
    msg["From"] = sender
    msg["To"] = to
    part1 = MIMEText(html, "html")
    msg.attach(part1)
    return msg.as_string()

def send_email(sender, to, content):
    """
    Sends email.
    """
    smtp = smtplib.SMTP("localhost")
    smtp.sendmail(sender, to, content)
    smtp.close()


def create_list_of_inconsistencies(template_file):
    """
    Creates a list of inconsistent messages that should be investigated.
    """
    template = polib.pofile(template_file)
    distance_matrix = get_distance_matrix(template)
    close = get_close_tuples(distance_matrix, 2)
    with_diffs = annotate_with_diffs(close)
    return filter_out(with_diffs, false_positives)
    
def main(template_file, from_email, to_email):
    filtered = create_list_of_inconsistencies(template_file)
    html = "<html><body>"
    for i in xrange(1, len(filtered)):
        html += create_document(filtered[i], documentbuilder.HtmlDocumentBuilder())
    html += "</body></html>"
    send_email(from_email, to_email, create_email(from_email, to_email, html))

if __name__ == "__main__":
    args = sys.argv[1:]
    if len(args) != 3:
        print """
        Usage: distances.py template.pot from_email to_email
        """
    main(args[0], args[1], args[2])

