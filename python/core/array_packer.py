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
 *    @Alain-Godo - Typing and formatting. Fixing boolean and string array
                    packing/unpacking issues.
 *******************************************************************************"""


import struct
from typing import Iterable, List, Union

# /********************************************************************************
# * Purpose of the module is to provide helper function for encoding and decoding
# * of Array Types ( 22 - 34 ) according to SparkPlug B Specification
# *
# * The module uses built-in struct module for packing and unpacking of bytes
# ********************************************************************************/


# Packing template function using in-built struct module
def convert_to_packed_bytes(array: Iterable[Union[int, float, bool, str]],
                            format_specifier: str) -> bytes:
    packed_bytes = struct.pack('<{}{}'.format(len(array), format_specifier),
                               *array)
    return packed_bytes


# Functions for packing each type of array as mentioned in the SparkPlug B Specification
def convert_to_packed_int8_array(array: Iterable[int]) -> bytes:
    return convert_to_packed_bytes(array, 'b')


def convert_to_packed_int16_array(array: Iterable[int]) -> bytes:
    return convert_to_packed_bytes(array, 'h')


def convert_to_packed_int32_array(array: Iterable[int]) -> bytes:
    return convert_to_packed_bytes(array, 'i')


def convert_to_packed_int64_array(array: Iterable[int]) -> bytes:
    return convert_to_packed_bytes(array, 'q')


def convert_to_packed_uint8_array(array: Iterable[int]) -> bytes:
    return convert_to_packed_bytes(array, 'B')


def convert_to_packed_uint16_array(array: Iterable[int]) -> bytes:
    return convert_to_packed_bytes(array, 'H')


def convert_to_packed_uint32_array(array: Iterable[int]) -> bytes:
    return convert_to_packed_bytes(array, 'I')


def convert_to_packed_uint64_array(array: Iterable[int]) -> bytes:
    return convert_to_packed_bytes(array, 'Q')


def convert_to_packed_float_array(array: Iterable[float]) -> bytes:
    return convert_to_packed_bytes(array, 'f')


def convert_to_packed_double_array(array: Iterable[float]) -> bytes:
    return convert_to_packed_bytes(array, 'd')


def convert_to_packed_boolean_array(array: Iterable[bool]) -> bytes:
    # convert the boolean values to a binary string and right pad with zeros
    binary_string = ''.join(str(b) for b in array).ljust(8 * ((len(array) + 7) // 8), '0')
    # pack the boolean values into a byte array
    packed_bytes = bytes(int(binary_string[i:i + 8], 2) for i in range(0, len(binary_string), 8))
    # pack the length of the boolean array and the boolean array
    return struct.pack("<I", len(array)) + packed_bytes


def convert_to_packed_string_array(array: Iterable[str]) -> bytes:
    # convert the strings to a byte array
    packed_bytes = '\x00'.join(array).encode('utf-8') + b'\x00'
    return packed_bytes


def convert_to_packed_datetime_array(array: Iterable[int]) -> bytes:
    # convert received epoch time to 8-byte (int64) array
    packed_bytes = convert_to_packed_int64_array(array)
    return packed_bytes


# Un-packing template function
def convert_from_packed_bytes(packed_bytes: bytes,
                              format_specifier: str,
                              length: int) -> List[Union[int, float, bool, str]]:
    return list(struct.unpack('<{}{}'.format(length, format_specifier), packed_bytes))


# Functions for un-packing packed byte arrays for every type
def convert_from_packed_int8_array(packed_bytes: bytes) -> List[int]:
    return convert_from_packed_bytes(packed_bytes, 'b', len(packed_bytes))


def convert_from_packed_int16_array(packed_bytes: bytes) -> List[int]:
    return convert_from_packed_bytes(packed_bytes, 'h', len(packed_bytes) // 2)


def convert_from_packed_int32_array(packed_bytes: bytes) -> List[int]:
    return convert_from_packed_bytes(packed_bytes, 'i', len(packed_bytes) // 4)


def convert_from_packed_int64_array(packed_bytes: bytes) -> List[int]:
    return convert_from_packed_bytes(packed_bytes, 'q', len(packed_bytes) // 8)


def convert_from_packed_uint8_array(packed_bytes: bytes) -> List[int]:
    return convert_from_packed_bytes(packed_bytes, 'B', len(packed_bytes))


def convert_from_packed_uint16_array(packed_bytes: bytes) -> List[int]:
    return convert_from_packed_bytes(packed_bytes, 'H', len(packed_bytes) // 2)


def convert_from_packed_uint32_array(packed_bytes: bytes) -> List[int]:
    return convert_from_packed_bytes(packed_bytes, 'I', len(packed_bytes) // 4)


def convert_from_packed_uint64_array(packed_bytes: bytes) -> List[int]:
    return convert_from_packed_bytes(packed_bytes, 'Q', len(packed_bytes) // 8)


def convert_from_packed_float_array(packed_bytes: bytes) -> List[float]:
    return convert_from_packed_bytes(packed_bytes, 'f', len(packed_bytes) // 4)


def convert_from_packed_double_array(packed_bytes: bytes) -> List[float]:
    return convert_from_packed_bytes(packed_bytes, 'd', len(packed_bytes) // 8)


def convert_from_packed_boolean_array(packed_bytes: bytes) -> List[bool]:
    # unpack the 4-byte integer representing the number of boolean values
    boolean_count, = struct.unpack("<I", packed_bytes[:4])
    # left pad with zeros and unpack the packed bytes into a list of booleans
    return [int(bit) for bit in ''.join(f'{byte:08b}' for byte in packed_bytes[4:])[:boolean_count]]


def convert_from_packed_string_array(packed_bytes: bytes) -> List[str]:
    # packed bytes are decoded and stripped of null characters
    return packed_bytes.decode('utf-8').split('\x00')[:-1]


def convert_from_packed_datetime_array(packed_bytes: bytes) -> List[int]:
    # unpack the packed bytes the result will be epoch values
    epoch_array: List[int] = convert_from_packed_int64_array(packed_bytes)
    # epoch milliseconds are returned as is
    return epoch_array
