# To generate the base protobuf sparkplug_b Python library
protoc -I=../../sparkplug_b/ --python_out=. ../../sparkplug_b/sparkplug_b.proto 
