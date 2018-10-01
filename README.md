# [MECE](https://en.wikipedia.org/wiki/MECE_principle)LA - Mutually Exclusive Collectively Exhaustive Log Analyzer

## The problem

You come to an old legacy system with no documentation and want to know what's
going on. You find the log file. There are thousands of lines in the log, possibly
several MB of data and the structure is not self evident.

### The pain of traditional unix solution

You `grep` first pattern you see in the log. `grep` returns a few hundred lines.
Nice. You `grep` another pattern and `grep` presents you with another couple
hundred lines. Does the second group overlap with the first? Hard to tell. So
you `grep -v` the patterns or `grep -e first-pattern -e second-pattern` to
separate logs into [MECE](https://en.wikipedia.org/wiki/MECE_principle) groups. After separating the logs into 10 MECE files
and examining the lines still remaining in the log you realize that *first-pattern* needs a little
tweeking. Now if you could only re-do all the separation and greping with the
eleven updated patterns again... and repeat this painful process untill the whole log
is neatly separated to Mutually Exclusive and Collectively Exhaustive groups.


## Usage

You have your log file to analyze (say `/var/log/authlog` for this example).
You create file that will hold your regex rules (say `sample.regex` for
example, but any name will do)

Run `mecela -l /var/log/authlog -r sample.regex`.

Now in different terminal open sample.regex, add another line or change an
existing one and save.

In a few msec to a few minutes you should see the new results in the first
terminal running mecela.

Repeat until mecela writes your rules are
[MECE](https://en.wikipedia.org/wiki/MECE_principle). Now you can be sure you have
categorized all lines in the log file to your patterns. and hopefully your
undestanding of the legacy system is much greater than when you started.

### Format of regex file

Each line defines a pattern.
First word on the line is a name of the pattern. Anything is fine, just don't
put there any spaces.
Anything after the first space till the end of line is regex to be searched in
the log file. It can be as simple as an arbitrary string from the log (so long
as it does  not contain any `\`, `(` or `{`) or it can be any
[Java style regex](https://docs.oracle.com/javase/10/docs/api/java/util/regex/Pattern.html).
