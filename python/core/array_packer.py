"""*******************************************************************************
 * Copyright (c) 2021 Ian Craggs
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * and Eclipse Distribution License v1.0 which accompany this distribution. 
 *
 * The Eclipse Public License is available at 
 *    https://www.eclipse.org/legal/epl-2.0/
 * and the Eclipse Distribution License is available at 
 *   http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *    @rahulrauki - inital Array packing and unpacking as per SparkPlug B guidelines
 *******************************************************************************"""


import struct

#/********************************************************************************
# * Purpose of the module is to provide helper function for encoding and decoding
# * of Array Types ( 22 - 34 ) according to SparkPlug B Specification 
# *
# * The module uses built-in struct module for packing and unpacking of bytes
# ********************************************************************************/

# Packing template function using in-built struct module
def convert_to_packed_bytes(array, format_specifier):
    packed_bytes = struct.pack('<{}{}'.format(len(array), format_specifier), *array)
    return packed_bytes

# Functions for packing each type of array as mentioned in the SparkPlug B Specification
def convert_to_packed_int8_array(array):
    return convert_to_packed_bytes(array, 'b')

def convert_to_packed_int16_array(array):
    return convert_to_packed_bytes(array, 'h')

def convert_to_packed_int32_array(array):
    return convert_to_packed_bytes(array, 'i')

def convert_to_packed_int64_array(array):
    return convert_to_packed_bytes(array, 'q')

def convert_to_packed_uint8_array(array):
    return convert_to_packed_bytes(array, 'B')

def convert_to_packed_uint16_array(array):
    return convert_to_packed_bytes(array, 'H')

def convert_to_packed_uint32_array(array):
    return convert_to_packed_bytes(array, 'I')

def convert_to_packed_uint64_array(array):
    return convert_to_packed_bytes(array, 'Q')

def convert_to_packed_float_array(array):
    return convert_to_packed_bytes(array, 'f')

def convert_to_packed_double_array(array):
    return convert_to_packed_bytes(array, 'd')

def convert_to_packed_boolean_array(boolean_array):
    # calculate the number of packed bytes required
    packed_bytes_count = (len(boolean_array) + 7) // 8
    # convert the boolean array into a packed byte string
    packed_bytes = bytearray(packed_bytes_count)
    for i, value in enumerate(boolean_array):
        packed_bytes[i // 8] |= value << (i % 8)
    # return the packed bytes preceded by a 4-byte integer representing the number of boolean values
    return struct.pack("<I", len(boolean_array)) + packed_bytes

def convert_to_packed_string_array(array):
    # convert strings to bytes and encode to hex
    hex_string_array = [string.encode().hex() for string in array]
    # convert hex string to bytes and terminate with null character
    packed_bytes = [bytes(hex_string, 'utf-8') + b'\x00' for hex_string in hex_string_array]
    # joining the bytes to form a null terminated byte string
    return b''.join(packed_bytes)

def convert_to_packed_datetime_array(array):
    # convert receievd epoch time to 8-byte (int64) array
    packed_bytes = convert_to_packed_int64_array(array)
    return packed_bytes



# Un-packing template function
def convert_from_packed_bytes(packed_bytes, format_specifier, length):
    return struct.unpack('<{}{}'.format(length, format_specifier), packed_bytes)

# Functions for un-packing packed byte arrays for every type
def convert_from_packed_int8_array(packed_bytes):
    return convert_from_packed_bytes(packed_bytes, 'b', len(packed_bytes))

def convert_from_packed_int16_array(packed_bytes):
    return convert_from_packed_bytes(packed_bytes, 'h', len(packed_bytes) // 2)

def convert_from_packed_int32_array(packed_bytes):
    return convert_from_packed_bytes(packed_bytes, 'i', len(packed_bytes) // 4)

def convert_from_packed_int64_array(packed_bytes):
    return convert_from_packed_bytes(packed_bytes, 'q', len(packed_bytes) // 8)

def convert_from_packed_uint8_array(packed_bytes):
    return convert_from_packed_bytes(packed_bytes, 'B', len(packed_bytes))

def convert_from_packed_uint16_array(packed_bytes):
    return convert_from_packed_bytes(packed_bytes, 'H', len(packed_bytes) // 2)

def convert_from_packed_uint32_array(packed_bytes):
    return convert_from_packed_bytes(packed_bytes, 'I', len(packed_bytes) // 4)

def convert_from_packed_uint64_array(packed_bytes):
    return convert_from_packed_bytes(packed_bytes, 'Q', len(packed_bytes) // 8)

def convert_from_packed_float_array(packed_bytes):
    return convert_from_packed_bytes(packed_bytes, 'f', len(packed_bytes) // 4)

def convert_from_packed_double_array(packed_bytes):
    return convert_from_packed_bytes(packed_bytes, 'd', len(packed_bytes) // 8)

def convert_from_packed_boolean_array(packed_bytes):
    # unpack the 4-byte integer representing the number of boolean values
    boolean_count, = struct.unpack("<I", packed_bytes[:4])
    # unpack the packed bytes into a list of booleans
    boolean_array = []
    for i in range(boolean_count):
        # True is represented by 1 and False by 0 in the array
        boolean_array.append((packed_bytes[4 + i // 8] >> (i % 8)) & 1)
    return boolean_array

def convert_from_packed_string_array(packed_bytes):
    string_array = []
    # packed bytes are decoded and stripped of null characters
    decoded_hex_string = packed_bytes.decode('utf-8').split('\x00')
    for hex_string in decoded_hex_string:
        # resulting hex string is converted to byte and then to decoded to strings
        string_array.append(bytes.fromhex(hex_string).decode())
    return string_array

def convert_from_packed_datetime_array(packed_bytes):
    # unpack the packed bytes the result will be epoch values
    epoch_array = convert_from_packed_int64_array(packed_bytes)
    # epoch milliseconds are returned as is
    return epoch_array