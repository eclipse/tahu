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

#!/bin/sh

#echo "Running static example..."
#./test/test_static

echo ""
echo "Running dynamic example..."
#echo "Starting LD_LIBRARY_PATH:  ${LD_LIBRARY_PATH}"
PWD=`pwd`
export LD_LIBRARY_PATH=${LD_LIBRARY_PATH}:${PWD}/lib
#echo "New LD_LIBRARY_PATH:       ${LD_LIBRARY_PATH}"
./test/test_dynamic
