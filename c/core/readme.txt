#/********************************************************************************
# * Copyright (c) 2014-2019 Cirrus Link Solutions and others
# *
# * This program and the accompanying materials are made available under the
# * terms of the Eclipse Public License 2.0 which is available at
# * http://www.eclipse.org/legal/epl-2.0.
# *
# * SPDX-License-Identifier: EPL-2.0
# *
# * Contributors:
# *   Cirrus Link Solutions - initial implementation
# ********************************************************************************/

# To generate the base protobuf tahu NanoPB C library (using Protoc v2.6.1 and Nanopb v0.3.5)
protoc --proto_path=../../ -otahu.pb ../../sparkplug_b/sparkplug_b.proto 
~/nanopb/nanopb-0.3.5-linux-x86/generator/nanopb_generator.py -f tahu.options tahu.pb
mv tahu.pb src/
mv tahu.pb.c src/
mv tahu.pb.h include/
