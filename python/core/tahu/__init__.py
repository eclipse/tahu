"""
Core SparkplugB/MQTT library from Eclipse
"""

#/********************************************************************************
# * Copyright (c) 2014, 2018, 2020, 2022 Cirrus Link Solutions and others
# *
# * This program and the accompanying materials are made available under the
# * terms of the Eclipse Public License 2.0 which is available at
# * http://www.eclipse.org/legal/epl-2.0.
# *
# * SPDX-License-Identifier: EPL-2.0
# *
# * Contributors:
# *   Cirrus Link Solutions - initial implementation
# *   Justin Brzozoski @ SignalFire Wireless Telemetry - major rewrite
# ********************************************************************************/

import time
import enum
from . import sparkplug_b_pb2
from typing import *

class SparkplugDecodeError(ValueError):
    """Exception type for all errors related to decoding SparkplugB payloads"""
    pass


class DataType(enum.IntEnum):
    """Enumeration of all SparkplugB datatype values"""
    Unknown = sparkplug_b_pb2.Unknown
    Int8 = sparkplug_b_pb2.Int8
    Int16 = sparkplug_b_pb2.Int16
    Int32 = sparkplug_b_pb2.Int32
    Int64 = sparkplug_b_pb2.Int64
    UInt8 = sparkplug_b_pb2.UInt8
    UInt16 = sparkplug_b_pb2.UInt16
    UInt32 = sparkplug_b_pb2.UInt32
    UInt64 = sparkplug_b_pb2.UInt64
    Float = sparkplug_b_pb2.Float
    Double = sparkplug_b_pb2.Double
    Boolean = sparkplug_b_pb2.Boolean
    String = sparkplug_b_pb2.String
    DateTime = sparkplug_b_pb2.DateTime
    Text = sparkplug_b_pb2.Text
    UUID = sparkplug_b_pb2.UUID
    DataSet = sparkplug_b_pb2.DataSet
    Bytes = sparkplug_b_pb2.Bytes
    File = sparkplug_b_pb2.File
    Template = sparkplug_b_pb2.Template
    PropertySet = sparkplug_b_pb2.PropertySet
    PropertySetList = sparkplug_b_pb2.PropertySetList


def _get_type_from_datatype(datatype: DataType) -> Type:
    """Return the best Python type to handle a SparkplugB DataType if one exists, raises ValueError otherwise"""
    # TODO - Figure out the best way to handle the complex types in this list.
    # For now, they are commented out to indicate there is no native Python type.
    PYTHON_TYPE_PER_DATATYPE = {
        #DataType.Unknown : None,
        DataType.Int8: int,
        DataType.Int16: int,
        DataType.Int32: int,
        DataType.Int64: int,
        DataType.UInt8: int,
        DataType.UInt16: int,
        DataType.UInt32: int,
        DataType.UInt64: int,
        DataType.Float: float,
        DataType.Double: float,
        DataType.Boolean: bool,
        DataType.String: str,
        DataType.DateTime: int,
        DataType.Text: str,
        DataType.UUID: str,
        #DataType.DataSet : DataSet,
        DataType.Bytes: bytes,
        DataType.File: bytes,
        #DataType.Template : lambda x : x,
        #DataType.PropertySet : lambda x : x,
        #DataType.PropertySetList : lambda x : x,
    }
    if datatype not in PYTHON_TYPE_PER_DATATYPE:
        raise ValueError(f'DataType {datatype} not fully supported')
    return PYTHON_TYPE_PER_DATATYPE[datatype]

def _get_datatype_from_type(pytype: Type) -> DataType:
    """Return the best SparkplugB DataType type to handle a Python type if one exists, raises ValueError otherwise"""
    DATATYPE_PER_PYTHON_TYPE = {
        int: DataType.Int64,
        float: DataType.Double,
        bool: DataType.Boolean,
        str: DataType.String,
        bytes: DataType.Bytes,
    }
    if pytype not in DATATYPE_PER_PYTHON_TYPE:
        raise ValueError(f'No good Sparkplug type for Python type {pytype}')
    return DATATYPE_PER_PYTHON_TYPE[pytype]

def _get_usable_value_fields_for_datatype(datatype: DataType) -> Set[str]:
    """Return a set of "oneof value" field names that we are willing to read a value from for a given SparkplugB DataType"""
    # NOTE: This is not normative by spec, but is useful when talking to an imperfect
    # implementation on the other side.  It lists for each expected datatype
    # which value fields we will try and read from without complaint when
    # we receive a payload.
    CONVERTIBLE_VALUE_FIELD_PER_DATATYPE = {
        #DataType.Unknown: set(),
        DataType.Int8: set(('int_value', 'long_value', 'boolean_value')),
        DataType.Int16: set(('int_value', 'long_value', 'boolean_value')),
        DataType.Int32: set(('int_value', 'long_value', 'boolean_value')),
        DataType.Int64: set(('int_value', 'long_value', 'boolean_value')),
        DataType.UInt8: set(('int_value', 'long_value', 'boolean_value')),
        DataType.UInt16: set(('int_value', 'long_value', 'boolean_value')),
        DataType.UInt32: set(('int_value', 'long_value', 'boolean_value')),
        DataType.UInt64: set(('int_value', 'long_value', 'boolean_value')),
        DataType.Float: set(('float_value', 'double_value')),
        DataType.Double: set(('float_value', 'double_value')),
        DataType.Boolean: set(('int_value', 'long_value', 'boolean_value')),
        DataType.String: set(('string_value')),
        DataType.DateTime: set(('long_value')),
        DataType.Text: set(('string_value')),
        DataType.UUID: set(('string_value')),
        DataType.DataSet: set(('dataset_value')),
        DataType.Bytes: set(('bytes_value')),
        DataType.File: set(('bytes_value')),
        DataType.Template: set(('template_value')),
    }
    return CONVERTIBLE_VALUE_FIELD_PER_DATATYPE.get(datatype, set())

def _is_int_datatype(datatype: DataType) -> bool:
    """Return whether SparkplugB DataType is an integer type"""
    return (datatype in (DataType.Int8, DataType.UInt8, DataType.Int16,
                         DataType.UInt16, DataType.Int32, DataType.UInt32,
                         DataType.Int64, DataType.UInt64))

def _get_min_max_limits_per_int_datatype(datatype: DataType) -> Tuple[int, int]:
    """Return a tuple with "allowable" (min, max) range for a given integer SparkplugB DataType"""
    # I could not find these constant limits in Python ...
    # It's not in ctypes or anywhere else AFAIK!
    MIN_MAX_LIMITS_PER_INTEGER_DATATYPE = {
        DataType.Int8: (-128, 127),
        DataType.UInt8: (0, 255),
        DataType.Int16: (-32768, 32767),
        DataType.UInt16: (0, 65535),
        DataType.Int32: (-2147483648, 2147483647),
        DataType.UInt32: (0, 4294967295),
        DataType.Int64: (-9223372036854775808, 9223372036854775807),
        DataType.UInt64: (0, 18446744073709551615),
    }
    return MIN_MAX_LIMITS_PER_INTEGER_DATATYPE[datatype]

def timestamp_to_sparkplug(utc_seconds: Optional[float] = None) -> int:
    """
    Convert a timestamp to SparkplugB DateTime value

    If called without a parameter, uses the current system time.

    To convert a Python datetime object, pass in the datetime.timestamp like this:

    sample_datetime = datetime(2006, 11, 21, 16, 30, tzinfo=timezone.utc)
    sparkplug_b.timestamp_to_sparkplug(sample_datetime.timestamp())

    :param utc_seconds: seconds since Unix epoch UTC (optional, default=current time)

    """
    if utc_seconds is None:
        utc_seconds = time.clock_gettime(time.CLOCK_REALTIME)
    return int(utc_seconds * 1000)

def timestamp_from_sparkplug(sparkplug_time: float) -> float:
    """
    Convert a SparkplugB DateTime value to a timestamp

    To convert back to a Python datetime object, use the output with datetime.fromtimestamp like this:

    datetime.fromtimestamp(sparkplug_b.timestamp_from_sparkplug(value), timezone.utc)

    :param sparkplug_time: SparkplugB DateTime value

    """
    return (float(sparkplug_time) / 1000.0)

SparkplugValueContainer = Union[sparkplug_b_pb2.Payload.Template.Parameter, sparkplug_b_pb2.Payload.DataSet.DataSetValue, sparkplug_b_pb2.Payload.PropertyValue, sparkplug_b_pb2.Payload.Metric]

def value_to_sparkplug(container: SparkplugValueContainer, datatype: DataType, value: Any, u32_in_long: bool = False) -> None:
    """
    Help pass a value into a payload container in preparation of protobuf packing

    Several structure types in the SparkplugB protobuf definition contain "oneof value" structs within them.
    This function helps pass a value into one of those structures, using the correct oneof sub-field and data conversion or casting rules, based on the other parameters.

    Will raise ValueError if the datatype requested cannot be handled

    :param container: a Sparkplug Payload.Template.Parameter, Payload.DataSet.DataSetValue, Payload.PropertyValue, or Payload.Metric message object to fill in
    :param datatype: the Sparkplug DataType of the value
    :param value: the value to store
    :param u32_in_long: whether to put UInt32 DataType in long_value or int_value (Default value = False)
    """
    # The Sparkplug B protobuf schema doesn't make use of signed ints.
    # We have to do byte-casting because of this when handling anything signed.
    # Tests well against Ignition 8.1.1 with u32_in_long=True
    # TODO - Add is_null support if value is None
    # TODO - Should we clamp any outgoing values larger than the datatype supports?
    if u32_in_long and datatype == DataType.UInt32:
        container.long_value = value
    elif datatype in [DataType.Int8, DataType.Int16, DataType.Int32]:
        bytes = int(value).to_bytes(4, 'big', signed=True)
        container.int_value = int().from_bytes(bytes, 'big', signed=False)
    elif datatype == DataType.Int64:
        bytes = int(value).to_bytes(8, 'big', signed=True)
        container.long_value = int().from_bytes(bytes, 'big', signed=False)
    elif datatype in [DataType.UInt8, DataType.UInt16, DataType.UInt32]:
        container.int_value = value
    elif datatype in [DataType.UInt64, DataType.DateTime]:
        container.long_value = value
    elif datatype == DataType.Float:
        container.float_value = value
    elif datatype == DataType.Double:
        container.double_value = value
    elif datatype == DataType.Boolean:
        container.boolean_value = value
    elif datatype in [DataType.String, DataType.Text, DataType.UUID]:
        container.string_value = value
    elif isinstance(container, sparkplug_b_pb2.Payload.Metric) and (datatype in [DataType.Bytes, DataType.File]):
        container.bytes_value = value
    elif isinstance(container, sparkplug_b_pb2.Payload.Metric) and (datatype == DataType.Template):
        value.to_sparkplug_template(container.template_value, u32_in_long)
    elif isinstance(container, sparkplug_b_pb2.Payload.Metric) and (datatype == DataType.DataSet):
        value.to_sparkplug_dataset(container.dataset_value, u32_in_long)
    else:
        raise ValueError(f'Unhandled datatype={datatype} for container={type(container)} in value_to_sparkplug')

def value_from_sparkplug(container: SparkplugValueContainer, datatype: DataType) -> Any:
    """
    Help read a value out of a payload container after protobuf unpacking

    Several structure types in the SparkplugB protobuf definition contain "oneof value" structs within them.
    This function helps read a value from one of those structures, using the correct oneof sub-field and data conversion or casting rules, based on the other parameters.

    Returns the value in an appropriate Python datatype

    Will raise SparkplugDecodeError if the "oneof value" portion of the payload is not setup properly or the datatype cannot be handled.

    :param container: a Sparkplug Payload.Template.Parameter, Payload.DataSet.DataSetValue, Payload.PropertyValue, or Payload.Metric message object to read from
    :param datatype: the Sparkplug DataType of the value

    """
    # The Sparkplug B protobuf schema doesn't make use of signed ints.
    # We have to do byte-casting because of this when handling anything signed.
    # We try to be flexible when handling incoming values because there are some bad
    # implementations out there that might use the wrong value field.
    # We clamp values on any incoming integers larger than the datatype supports.
    # Tests well against Ignition 8.1.1
    if isinstance(container, sparkplug_b_pb2.Payload.PropertyValue) or isinstance(container, sparkplug_b_pb2.Payload.Metric):
        if container.HasField('is_null') and container.is_null:
            return None
    value_field = container.WhichOneof('value')
    if value_field is None:
        raise SparkplugDecodeError('No value field present')
    if value_field not in _get_usable_value_fields_for_datatype(datatype):
        raise SparkplugDecodeError(f'Unexpected value field {value_field} for datatype {datatype}')
    value = getattr(container, value_field)
    if _is_int_datatype(datatype):
        value_min, value_max = _get_min_max_limits_per_int_datatype(datatype)
        if value_min < 0:
            # If we're expecting a signed value, we need to cast if reading from int_value or long_value
            # since they are unsigned in the protobuf
            if value_field == 'int_value':
                bytes = value.to_bytes(4, 'big', signed=False)
                value = int().from_bytes(bytes, 'big', signed=True)
            elif value_field == 'long_value':
                bytes = value.to_bytes(8, 'big', signed=False)
                value = int().from_bytes(bytes, 'big', signed=True)
        # Now we clamp them to the limits
        if value < value_min:
            value = value_min
        elif value > value_max:
            value = value_max
    if datatype == DataType.DataSet:
        return DataSet.from_sparkplug_dataset(value)
    pytype = _get_type_from_datatype(datatype)
    if pytype is not None:
        return pytype(value)
    raise SparkplugDecodeError(f'Unhandled datatype={datatype} in value_from_sparkplug')

def mqtt_params(server: str, 
                port: Optional[int] = None,
                username: Optional[str] = None, 
                password: Optional[str] = None, 
                client_id: Optional[str] = None, 
                keepalive: int = 60,
                tls_enabled: bool = False, 
                ca_certs: Optional[str] = None, 
                certfile: Optional[str] = None, 
                keyfile: Optional[str] = None) -> Dict[str, Any]:
    """
    Collect all setup parameters for a single MQTT connection into a object to be used when initializing a Node

    Most of these parameters are simply passed to the relevant Paho MQTT API.
    See https://pypi.org/project/paho-mqtt/ for more explanation.

    :param server: hostname or IP address or MQTT server
    :param port: TCP port (optional: defaults to 1883 or 8883 depending on tls_enabled)
    :param username: username (optional, defaults to None)
    :param password: password (optional, defaults to None)
    :param client_id: client ID (optional, defaults to "<group_id>_<edge_id>_<pid>" for edge nodes)
    :param keepalive: keepalive seconds (optional, defaults to 60)
    :param tls_enabled: whether to enable SSL/TLS (optional, defaults to False)
    :param ca_certs: a string path to the Certificate Authority certificate files that are to be treated as trusted by this client (optional, defaults to None)
    :param certfile: strings pointing to the PEM encoded client certificate (optional, defaults to None)
    :param keyfile: strings pointing to the PEM encoded client private keys (optional, defaults to None)
    """
    mqtt_params: Dict[str, Any] = {}
    mqtt_params['client_id'] = client_id
    mqtt_params['server'] = server
    mqtt_params['port'] = port if port else (8883 if tls_enabled else 1883)
    mqtt_params['username'] = username
    mqtt_params['password'] = password
    mqtt_params['keepalive'] = keepalive
    mqtt_params['tls_enabled'] = tls_enabled
    mqtt_params['ca_certs'] = ca_certs
    mqtt_params['certfile'] = certfile
    mqtt_params['keyfile'] = keyfile
    return mqtt_params


# TODO - Create a template object type

class DataSet(object):
    """DataSet: object for working with SparkplugB dataset values"""

    # TODO - Add methods to allow easy value access by indices, e.g. with DataSet D you could just reference D[0][0] or D[0][column_name]

    def __init__(self, name_datatype_tuples: Dict[str, int]) -> None:
        self._num_columns = len(name_datatype_tuples)
        if self._num_columns == 0:
            raise ValueError('dataset must have at least one column')
        self._column_names = [str(n) for n in name_datatype_tuples.keys()]
        self._column_datatypes = [DataType(d) for d in name_datatype_tuples.values()]
        self._data: List[List] = []

    def add_rows(self, 
                 data: Union[List, Dict], 
                 keyed: bool = False, 
                 in_columns: bool = False,
                 insert_index: Optional[int] = None) -> None:
        """
        Add rows to an existing DataSet object

        Takes in data in one of a few formats, and an optional int row index specifying where to insert the new rows.

        Modifies the (mutable) dataset object being operated on. Returns nothing.

        :param data: new data rows to add to the dataset
        :param keyed: whether to locate the columns via dict keys (True), or via ordered list index (False, default)
        :param in_columns: whether data goes down each column first (True), or across each row first (False, default)
        :param insert_index: which row index of the original dataset to insert the new data before; 0 means insert at beginning, default (None) means add to end

        Until I write better docs, here's some samples of the expected formats of data.

        Let's say you have three columns named 'A', 'B', 'C'.
        You want to push in data rows that would look like this in a tabular layout:
        A B C
        1 2 3
        4 5 6
        7 8 9

        Here's the different ways you could pass that in:
        keyed=False in_columns=False data=[[1,2,3],[4,5,6],[7,8,9]]
        keyed=True  in_columns=False data=[{'A':1, 'B':2, 'C':3},{'A':4, 'B':5, 'C':6},{'A':7, 'B':7, 'C':9}]
        keyed=False in_columns=True  data=[[1,4,7],[2,5,8],[3,6,9]]
        keyed=True  in_columns=True  data={'A':[1,4,7], 'B':[2,5,8], 'C':[3,6,9]}

        This convenience provided since I know you don't always have easy ways to get the data in
        one format or another, and you shouldn't have to waste any more of your time re-writing the same
        conversion functions over and over when I could just do it once for you.
        """
        if ((data is None) or (len(data) == 0)):
            return
        new_data = []
        col_keys = self._column_names if keyed else range(self._num_columns)
        col_python_types = [_get_type_from_datatype(self._column_datatypes[x]) for x in range(self._num_columns)]
        col_helper = tuple(zip(col_keys, col_python_types))
        if not in_columns:
            for row in data:
                new_row = []
                for k,t in col_helper:
                    new_row.append(t(row[k]))
                new_data.append(new_row)
        else:
            num_rows = len(data[col_keys[0]]) # type: ignore
            for k in col_keys[1:]:
                if len(data[k]) != num_rows: # type: ignore
                    raise ValueError(f'data does not have {num_rows} rows in all columns')
            for row_index in range(num_rows):
                new_row = []
                for k,t in col_helper:
                    new_row.append(t(data[k][row_index])) # type: ignore
                new_data.append(new_row)
        if insert_index:
            # This is a neat Python trick.
            # You can assign a new list to a slice of a list, and it will replace
            # the values within the slice with the values from the new list.
            # But if your slice is reduced to size 0, it will just insert the elements at that index.
            self._data[insert_index:insert_index] = new_data
        else:
            self._data.extend(new_data)

    def get_num_columns(self) -> int:
        """Return the number of columns in the DataSet"""
        return self._num_columns

    def get_num_rows(self) -> int:
        """Return the number of rows in the DataSet"""
        return len(self._data)

    def remove_rows(self, 
                    start_index: int = 0, 
                    end_index: Optional[int] = None, 
                    num_rows: Optional[int] = None) -> None:
        """
        Remove a contiguous set of rows from the DataSet

        :param start_index: first row to remove (optional, default=0)
        :param end_index: last row to remove (optional)
        :param num_rows: numer of rows to remove (optional)

        You should only provide one of end_index or num_rows, not both, else behavior is undefined.
        """
        if not end_index:
            end_index = (start_index
                         + num_rows) if num_rows else len(self._data)
        self._data[start_index:end_index] = []

    def get_rows(self, 
                 start_index: int = 0, 
                 end_index: Optional[int] = None, 
                 num_rows: Optional[int] = None, 
                 in_columns: bool = False, 
                 keyed: bool = False) -> Union[List, Dict]:
        """
        Returns a copy of the data from one or more rows in the DataSet

        See the comments on add_rows for an explanation of in_columns and keyed.

        You should only provide one of end_index or num_rows, not both, else behavior is undefined.

        :param start_index: index of first row (optional, default = 0)
        :param end_index: index of last row (optional)
        :param num_rows: number of rows to copy (optional)
        :param in_columns: whether data goes down each column first (True), or across each row first (False, default)
        :param keyed: whether to locate the columns via dict keys (True), or via ordered list index (False, default)

        """
        if not end_index:
            end_index = (start_index
                         + num_rows) if num_rows else len(self._data)
        if not in_columns:
            if keyed:
                return [dict(zip(self._column_names,
                                 row)) for row in self._data[start_index:end_index]]
            return self._data[start_index:end_index]
        if not keyed:
            listdata = []
            for k in range(self._num_columns):
                listdata.append([self._data[r][k] for r in range(start_index, end_index)])
            return listdata
        dictdata = {}
        for k in range(len(self._column_names)):
            dictdata[self._column_names[k]] = [self._data[r][k] for r in range(start_index, end_index)]
        return dictdata

    def to_sparkplug_dataset(self, sp_dataset: sparkplug_b_pb2.Payload.DataSet, u32_in_long: bool = False) -> sparkplug_b_pb2.Payload.DataSet:
        """
        Copy the DataSet into a SparkplugB Payload.DataSet

        :param sp_dataset: SparkplugB Payload.DataSet to modify
        :param u32_in_long: whether to put UInt32 DataType in long_value or int_value (Default value = False)
        """
        sp_dataset.num_of_columns = self._num_columns
        sp_dataset.columns.extend(self._column_names)
        sp_dataset.types.extend(self._column_datatypes)
        for data_row in self._data:
            sp_row = sp_dataset.rows.add()
            for c in range(self._num_columns):
                dataset_value = sp_row.elements.add()
                value_to_sparkplug(dataset_value, self._column_datatypes[c],
                                   data_row[c], u32_in_long)
        return sp_dataset

    @classmethod
    def from_sparkplug_dataset(cls, sp_dataset: sparkplug_b_pb2.Payload.DataSet) -> DataSet:
        """
        Create a new DataSet object from a SparkplugB Payload.DataSet

        Returns a new DataSet object

        :param sp_dataset: SparkplugB Payload.DataSet to copy from
        """
        try:
            new_dataset = cls(dict(zip(sp_dataset.columns, sp_dataset.types)))
        except ValueError as errmsg:
            raise SparkplugDecodeError(errmsg)
        for sp_row in sp_dataset.rows:
            new_row = []
            for c in range(new_dataset._num_columns):
                value = value_from_sparkplug(sp_row.elements[c],
                                             new_dataset._column_datatypes[c])
                new_row.append(value)
            new_dataset._data.append(new_row)
        return new_dataset

