# To generate the base protobuf sparkplug_b Java library
cd ~/dev/gitflow/Tahu/java/lib/core
protoc --proto_path=../../../sparkplug_b/ --java_out=src/main/java ../../../sparkplug_b/sparkplug_b.proto 
