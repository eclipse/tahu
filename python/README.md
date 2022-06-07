Add in "function annotations" or use "typing" module?

https://docs.python.org/3/tutorial/modules.html

# Python specific hints and tips

Basic instructions used to setup Python packaging found here: https://packaging.python.org/en/latest/tutorials/packaging-projects/

## Building PIP package

From in the `python` directory:

```
python3 -m build
```

## Install PIP package locally for testing

From in the `python` directory:

```
python3 -m pip install .
```

## Recompiling protobuf definition

From the top directory of tahu:

```
protoc -I=sparkplug_b --python_out=python/core/tahu sparkplug_b.proto
```

