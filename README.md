# Radon for Fabric

An alternative world storage format that tries to be more performant and durable than vanilla's own. This is hardly more
than a day's worth of work.

Do not use this on existing worlds. In fact, don't use it on any world -- Radon is still very experimental/unstable and
exists as a proof of concept for the time being. It remains to be seen if any of this is a good idea.

Right now, nothing here will work on the dedicated server because the project depends on LWJGL's bindings for Zstandard
and LMDB. In general the native library extraction and loading code is a huge hack -- contributions welcome to clean that
up.