# To generate the base protobuf sparkplug_b Java library
protoc --proto_path=../../ --java_out=src/main/java ../../sparkplug_b/sparkplug_b.proto
