# LimeWire source archive (CVS checkout, October 25, 2010)

This is a full checkout of LimeWire's public anonymous CVS repository
(`:pserver:guest@cvs.limewire.org:/cvs`), taken on October 25, 2010, the day before
LimeWire shut down under a court injunction. It is published for archeological
purposes, as a record of the Gnutella client that FrostWire descends from.

Nothing here has been modified. The `CVS/` folders in every directory and the
`CVSROOT/` admin files are kept as they were checked out, so the provenance of
every file can be traced back to the original server.

## License

LimeWire is free software from Lime Wire LLC, released under the GNU General
Public License, version 2 or (at your option) any later version. See
[`core/Copyright.txt`](core/Copyright.txt) and the headers in the source files.
Third-party code bundled in this tree keeps its own license, for example
`looks/`, `commons-pool/`, `httpclient-118/`, `vendor/fmj/`, `vendor/jaudiotagger/`
and `stubinstaller/Cryptlib/`.

## What was left out

One file is missing from this archive because it is larger than GitHub's 100 MB
file limit:

| File | Size | SHA-256 |
|---|---|---|
| `public/libtorrent/libtorrentwrapper/lib/osx/libtorrent-xgcc40-0_14_6.a` | 140,672,304 bytes | `5bff744a157e320f8bb2c7bfa46bf03bb1074ac351dcb3da70f0ecd658891021` |

It is a prebuilt macOS static build of libtorrent 0.14.6; the wrapper source that
links against it is still here. The macOS `.DS_Store` files and a local `.ivy2`
dependency cache from the machine that made the checkout were also left out.

## Layout

| Folder | What it is |
|---|---|
| `core/`, `components/` | The Gnutella core and the shared LimeWire components |
| `gui/`, `ui/`, `private-components/` | The Swing user interfaces |
| `public/` | Public modules: Mojito DHT tutorials, the libtorrent wrapper, inspection tools |
| `tests/` | Test suites |
| `lib/`, `vendor/` | Third-party libraries |
| `stubinstaller/` | The Windows stub installer |
| `CVS/`, `CVSROOT/` | CVS metadata from the original server |
