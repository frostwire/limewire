#!/bin/sh

# Converts:
# java.util.
#    List
#    ArrayList
#    LinkedList
#    Set
#    HashSet
#    Map
#    HashMap
#    Collections
#    Iterator
#    Comparator
#    Vector


rename() {
    echo "Fixing file: $2, import: $1"
    sed "s/import java.util.$1;.*$/import com.sun.java.util.collections.$1;/" < $2 > ${2}.tmp
    mv ${2}.tmp $2
}

du -a | awk '{print $2}' | sed 's/\/[^/]*$//' | grep -v "\.$" | grep -v "CVS$" | sed 's/^.\///' | ${CYGWIN_HOME}/bin/sort -u | while read X
do
    ls ${X}/*.java -1 | while read Y
    do
        rename List $Y
        rename ArrayList $Y
        rename LinkedList $Y
        rename Set $Y
        rename HashSet $Y
        rename Map $Y
        rename HashMap $Y
        rename Collections $Y
        rename Iterator $Y
        rename Comparator $Y
        rename Vector $Y
    done
done
