/********************************************************************************
 * Copyright (c) 2016-2018 Cirrus Link Solutions and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Cirrus Link Solutions - initial implementation
 ********************************************************************************/

/**
 * Provides support for generating Kura payloads.
 */
(function () {
    var ProtoBuf = require("protobufjs");

    var SparkplugPayload = ProtoBuf.parse("package org.eclipse.tahu.protobuf; message Payload { message Template { " +
            "message Parameter { optional string name = 1;optional uint32 type = 2; oneof value { uint32 int_value = 3; uint64 long_value = 4; " +
            "float float_value = 5; double double_value = 6; bool boolean_value = 7; string string_value = 8; ParameterValueExtension extension_value = 9; } " +
            "message ParameterValueExtension { extensions 1 to max; } } optional string version = 1; repeated Metric metrics = 2; " +
            "repeated Parameter parameters = 3; optional string template_ref = 4; optional bool is_definition = 5; extensions 6 to max; } " +
            "message DataSet { " +
            "message DataSetValue { oneof value { uint32 int_value = 1; uint64 long_value = 2; float float_value = 3; double double_value = 4; " +
            "bool boolean_value = 5; string string_value = 6; DataSetValueExtension extension_value = 7; } " +
            "message DataSetValueExtension { extensions 1 to max; } } " +
            "message Row { repeated DataSetValue elements = 1; extensions 2 to max; } optional uint64 num_of_columns = 1; repeated string columns = 2; " +
            "repeated uint32 types = 3; repeated Row rows = 4; extensions 5 to max; } " +
            "message PropertyValue { optional uint32 type = 1; optional bool is_null = 2;  oneof value { uint32 int_value = 3; uint64 long_value = 4; " +
            "float float_value = 5; double double_value = 6; bool boolean_value = 7; string string_value = 8; PropertySet propertyset_value = 9; " +
            "PropertySetList propertysets_value = 10; PropertyValueExtension extension_value = 11; } " +
            "message PropertyValueExtension { extensions 1 to max; } } " +
            "message PropertySet { repeated string keys = 1; repeated PropertyValue values = 2; extensions 3 to max; } " +
            "message PropertySetList { repeated PropertySet propertyset = 1; extensions 2 to max; } " +
            "message MetaData { optional bool is_multi_part = 1; optional string content_type = 2; optional uint64 size = 3; optional uint64 seq = 4; " +
            "optional string file_name = 5; optional string file_type = 6; optional string md5 = 7; optional string description = 8; extensions 9 to max; } " +
            "message Metric { optional string name = 1; optional uint64 alias = 2; optional uint64 timestamp = 3; optional uint32 datatype = 4; " +
            "optional bool is_historical = 5; optional bool is_transient = 6; optional bool is_null = 7; optional MetaData metadata = 8; " +
            "optional PropertySet properties = 9; oneof value { uint32 int_value = 10; uint64 long_value = 11; float float_value = 12; double double_value = 13; " +
            "bool boolean_value = 14; string string_value = 15; bytes bytes_value = 16; DataSet dataset_value = 17; Template template_value = 18; " +
            "MetricValueExtension extension_value = 19; } " +
            "message MetricValueExtension { extensions 1 to max; } } optional uint64 timestamp = 1; repeated Metric metrics = 2; optional uint64 seq = 3; " +
            "optional string uuid = 4; optional bytes body = 5; extensions 6 to max; } ").root,
        Payload = SparkplugPayload.lookup('org.eclipse.tahu.protobuf.Payload'),
        Template = SparkplugPayload.lookup('org.eclipse.tahu.protobuf.Payload.Template'),
        Parameter = SparkplugPayload.lookup('org.eclipse.tahu.protobuf.Payload.Template.Parameter'),
        DataSet = SparkplugPayload.lookup('org.eclipse.tahu.protobuf.Payload.DataSet'),
        DataSetValue = SparkplugPayload.lookup('org.eclipse.tahu.protobuf.Payload.DataSet.DataSetValue'),
        Row = SparkplugPayload.lookup('org.eclipse.tahu.protobuf.Payload.DataSet.Row'),
        PropertyValue = SparkplugPayload.lookup('org.eclipse.tahu.protobuf.Payload.PropertyValue'),
        PropertySet = SparkplugPayload.lookup('org.eclipse.tahu.protobuf.Payload.PropertySet'),
        PropertyList = SparkplugPayload.lookup('org.eclipse.tahu.protobuf.Payload.PropertyList'),
        MetaData =SparkplugPayload.lookup('org.eclipse.tahu.protobuf.Payload.MetaData'),
        Metric = SparkplugPayload.lookup('org.eclipse.tahu.protobuf.Payload.Metric');

    /**
     * Sets the value of an object given it's type expressed as an integer
     */
    setValue = function(type, value, object) {
        switch (type) {
            case 1: // Int8
            case 2: // Int16
            case 3: // Int32
            case 5: // UInt8
            case 6: // UInt32
                object.intValue = value;
                break;
            case 4: // Int64
            case 7: // UInt32
            case 8: // UInt64
            case 13: // DataTime
                object.longValue = value;
                break;
            case 9: // Float
                object.floatValue = value;
                break;
            case 10: // Double
                object.doubleValue = value;
                break;
            case 11: // Boolean
                object.booleanValue = value;
                break;
            case 12: // String
            case 14: // Text
            case 15: // UUID
                object.stringValue = value;
                break;
            case 16: // DataSet
                object.datasetValue = encodeDataSet(value);
                break;
            case 17: // Bytes
            case 18: // File
                object.bytesValue = value;
                break;
            case 19: // Template
                object.templateValue = encodeTemplate(value);
                break;
            case 20: // PropertySet
                object.propertysetValue = encodePropertySet(value);
                break;
            case 21:
                object.propertysetsValue = encodePropertySetList(value);
                break;
        } 
    }

    getValue = function(type, object) {
        switch (type) {
            case 1: // Int8
            case 2: // Int16
            case 3: // Int32
            case 5: // UInt8
            case 6: // UInt32
                return object.intValue;
            case 4: // Int64
            case 7: // UInt32
            case 8: // UInt64
            case 13: // DataTime
                return object.longValue;
            case 9: // Float
                return object.floatValue;
            case 10: // Double
                return object.doubleValue;
            case 11: // Boolean
                return object.booleanValue;
            case 12: // String
            case 14: // Text
            case 15: // UUID
                return object.stringValue;
            case 16: // DataSet
                return decodeDataSet(object.datasetValue);
            case 17: // Bytes
            case 18: // File
                return object.bytesValue;
            case 19: // Template
                return decodeTemplate(object.templateValue);
            case 20: // PropertySet
                return decodePropertySet(object.propertysetValue);
            case 21:
                return decodePropertySetList(object.propertysetsValue);
            default:
                return null;
        } 
    }

    encodeType = function(typeString) {
        switch (typeString.toUpperCase()) {
            case "INT8":
                return 1;
            case "INT16":
                return 2;
            case "INT32":
            case "INT":
                return 3;
            case "INT64":
            case "LONG":
                return 4;
            case "UINT8":
                return 5;
            case "UINT16":
                return 6;
            case "UINT32":
                return 7;
            case "UINT64":
                return 8;
            case "FLOAT":
                return 9;
            case "DOUBLE":
                return 10;
            case "BOOLEAN":
                return 11;
            case "STRING":
                return 12;
            case "DATETIME":
                return 13;
            case "TEXT":
                return 14;
            case "UUID":
                return 15;
            case "DATASET":
                return 16;
            case "BYTES":
                return 17;
            case "FILE":
                return 18;
            case "TEMPLATE":
                return 19;
            case "PROPERTYSET":
                return 20;
            case "PROPERTYSETLIST":
                return 21;
            default:
                return 0;
        }
    }

    decodeType = function(typeInt) {
        switch (typeInt) {
            case 1:
                return "Int8";
            case 2:
                return "Int16";
            case 3:
                return "Int32";
            case 4: 
                return "Int64";
            case 5:
                return "UInt8";
            case 6:
                return "UInt16";
            case 7:
                return "UInt32";
            case 8:
                return "UInt64";
            case 9:
                return "Float";
            case 10:
                return "Double";
            case 11:
                return "Boolean";
            case 12:
                return "String";
            case 14:
                return "Text";
            case 15:
                return "UUID";
            case 16:
                return "DataSet";
            case 17:
                return "Bytes";
            case 18:
                return "File";
            case 19:
                return "Template";
            case 20:
                return "PropertySet";
            case 21:
                return "PropertySetList";
        }
    }

    encodeTypes = function(typeArray) {
        var types = [];
        for (var i = 0; i < typeArray.length; i++) {
            types.push(encodeType(typeArray[i]));
        }
        return types;
    }

    decodeTypes = function(typeArray) {
        var types = [];
        for (var i = 0; i < typeArray.length; i++) {
            types.push(decodeType(typeArray[i]));
        }
        return types;
    }

    encodeDataSet = function(object) {
        var num = object.numOfColumns,
            names = object.columns,
            types = encodeTypes(object.types),
            rows = object.rows,
            newDataSet = DataSet.create({
                "numOfColumns" : num, 
                "columns" : object.columns, 
                "types" : types 
            }),
            newRows = [];
        // Loop over all the rows
        for (var i = 0; i < rows.length; i++) {
            var newRow = Row.create();
                row = rows[i];
                elements = [];
            // Loop over all the elements in each row
            for (var t = 0; t < num; t++) {
                var newValue = DataSetValue.create();
                setValue(types[t], row[t], newValue);
                elements.push(newValue);
            }
            newRow.elements = elements;
            newRows.push(newRow);
        }
        newDataSet.rows = newRows;
        return newDataSet;
    }

    decodeDataSet = function(protoDataSet) {
        var dataSet = {},
            protoTypes = protoDataSet.types,
            types = decodeTypes(protoTypes),
            protoRows = protoDataSet.rows,
            num = protoDataSet.numOfColumns,
            rows = [];
        
        // Loop over all the rows
        for (var i = 0; i < protoRows.length; i++) {
            var protoRow = protoRows[i],
                protoElements = protoRow.elements,
                row = [];
            // Loop over all the elements in each row
            for (var t = 0; t < num; t++) {
                row.push(getValue(protoTypes[t], protoElements[t]));
            }
            rows.push(row);
        }

        dataSet.numOfColumns = num;
        dataSet.types = types;
        dataSet.columns = protoDataSet.columns;
        dataSet.rows = rows;

        return dataSet;
    }

    encodeMetaData = function(object) {
        var metadata = MetaData.create(),
            isMultiPart = object.isMultiPart,
            contentType = object.contentType,
            size = object.size,
            seq = object.seq,
            fileName = object.fileName,
            fileType = object.fileType,
            md5 = object.md5,
            description = object.description;

        if (isMultiPart !== undefined && isMultiPart !== null) {
            metadata.isMultiPart = isMultiPart;
        }

        if (contentType !== undefined && contentType !== null) {
            metadata.contentType = contentType;
        }

        if (size !== undefined && size !== null) {
            metadata.size = size;
        }

        if (seq !== undefined && seq !== null) {
            metadata.seq = seq;
        }

        if (fileName !== undefined && fileName !== null) {
            metadata.fileName = fileName;
        }

        if (fileType !== undefined && fileType !== null) {
            metadata.fileType = fileType;
        }

        if (md5 !== undefined && md5 !== null) {
            metadata.md5 = md5;
        }

        if (description !== undefined && description !== null) {
            metadata.description = description;
        }

        return metadata;
    }

    decodeMetaData = function(protoMetaData) {
        var metadata = {},
            isMultiPart = protoMetaData.isMultiPart,
            contentType = protoMetaData.contentType,
            size = protoMetaData.size,
            seq = protoMetaData.seq,
            fileName = protoMetaData.fileName,
            fileType = protoMetaData.fileType,
            md5 = protoMetaData.md5,
            description = protoMetaData.description;

        if (isMultiPart !== undefined && isMultiPart !== null) {
            metadata.isMultiPart = isMultiPart;
        }

        if (contentType !== undefined && contentType !== null) {
            metadata.contentType = contentType;
        }

        if (size !== undefined && size !== null) {
            metadata.size = size;
        }

        if (seq !== undefined && seq !== null) {
            metadata.seq = seq;
        }

        if (fileName !== undefined && fileName !== null) {
            metadata.fileName = fileName;
        }

        if (fileType !== undefined && fileType !== null) {
            metadata.fileType = fileType;
        }

        if (md5 !== undefined && md5 !== null) {
            metadata.md5 = md5;
        }

        if (description !== undefined && description !== null) {
            metadata.description = description;
        }

        return metadata;
    }

    encodePropertyValue = function(object) {
        var type = encodeType(object.type),
            newPropertyValue = PropertyValue.create({
                "type" : type
            });

        if (object.value !== undefined && object.value === null) {
            newPropertyValue.isNull = true;
        }

        setValue(type, object.value, newPropertyValue);

        return newPropertyValue;
    }

    decodePropertyValue = function(protoValue) {
        var propertyValue = {};

        if (protoValue.isNull !== undefined && protoValue.isNull === true) {
            propertyValue.value = null;
        } else {
            propertyValue.value = getValue(protoValue.type, protoValue);
        }

        propertyValue.type = decodeType(protoValue.type);

        return propertyValue;
    }

    encodePropertySet = function(object) {
        var keys = [],
            values = [];

        for (var key in object) {
            if (object.hasOwnProperty(key)) {
                keys.push(key);
                values.push(encodePropertyValue(object[key]))  
            }
        }

        return PropertySet.create({
            "keys" : keys, 
            "values" : values
        });
    }

    decodePropertySet = function(protoSet) {
        var propertySet = {},
            protoKeys = protoSet.keys,
            protoValues = protoSet.values;

        for (var i = 0; i < protoKeys.length; i++) {
            propertySet[protoKeys[i]] = decodePropertyValue(protoValues[i]);
        }

        return propertySet;
    }

    encodePropertySetList = function(object) {
        var propertySets = [];
        for (var i = 0; i < object.length; i++) {
            propertySets.push(encodePropertySet(object[i]));
        }
        return PropertySetList.create({
            "propertySet" : propertySets
        });
    }

    decodePropertySetList = function(protoSets) {
        var propertySets = [];
        for (var i = 0; i < protoSets.length; i++) {
            propertySets.push(decodePropertySet(protoSets[i]));
        }
        return propertySets;
    }

    encodeParameter = function(object) {
        var type = encodeType(object.type),
            newParameter = Parameter.create({
                "name" : object.name, 
                "type" : type
            });
        setValue(type, object.value, newParameter);
        return newParameter;
    }

    decodeParameter = function(protoParameter) {
        var protoType = protoParameter.type,
            parameter = {};

        parameter.name = protoParameter.name;
        parameter.type = decodeType(protoType);
        parameter.value = getValue(protoType, protoParameter);

        return parameter;
    }

    encodeTemplate = function(object) {
        var template = Template.create(),
            metrics = object.metrics,
            parameters = object.parameters,
            isDef = object.isDefinition,
            ref = object.templateRef,
            version = object.version;

        if (version !== undefined && version !== null) {
            template.version = version;    
        }

        if (ref !== undefined && ref !== null) {
            template.templateRef = ref;    
        }

        if (isDef !== undefined && isDef !== null) {
            template.isDefinition = isDef;    
        }

        // Build up the metric
        if (object.metrics !== undefined && object.metrics !== null) {
            var newMetrics = []
                metrics = object.metrics;
            // loop over array of metrics
            for (var i = 0; i < metrics.length; i++) {
                newMetrics.push(encodeMetric(metrics[i]));
            }
            template.metrics = newMetrics;
        }

        // Build up the parameters
        if (object.parameters !== undefined && object.parameters !== null) {
            var newParameter = [];
            // loop over array of parameters
            for (var i = 0; i < object.parameters.length; i++) {
                newParameter.push(encodeParameter(object.parameters[i]));
            }
            template.parameters = newParameter;
        }

        return template;
    }

    decodeTemplate = function(protoTemplate) {
        var template = {},
            protoMetrics = protoTemplate.metrics,
            protoParameters = protoTemplate.parameters,
            isDef = protoTemplate.isDefinition,
            ref = protoTemplate.templateRef,
            version = protoTemplate.version;

        if (version !== undefined && version !== null) {
            template.version = version;    
        }

        if (ref !== undefined && ref !== null) {
            template.templateRef = ref;    
        }

        if (isDef !== undefined && isDef !== null) {
            template.isDefinition = isDef;    
        }

        // Build up the metric
        if (protoMetrics !== undefined && protoMetrics !== null) {
            var metrics = []
            // loop over array of proto metrics, decoding each one
            for (var i = 0; i < protoMetrics.length; i++) {
                metrics.push(decodeMetric(protoMetrics[i]));
            }
            template.metrics = metrics;
        }

        // Build up the parameters
        if (protoParameters !== undefined && protoParameters !== null) {
            var parameter = [];
            // loop over array of parameters
            for (var i = 0; i < protoParameters.length; i++) {
                parameter.push(decodeParameter(protoParameters[i]));
            }
            template.parameters = parameter;
        }

        return template;
    }

    encodeMetric = function(metric) {
        var newMetric = Metric.create({
                "name" : metric.name
            }),
            value = metric.value,
            datatype = encodeType(metric.type),
            alias = metric.alias,
            isHistorical = metric.isHistorical,
            isTransient = metric.isTransient,
            metadata = metric.metadata,
            timestamp = metric.timestamp,
            properties = metric.properties;
        
        // Get metric type and value
        newMetric.datatype = datatype;
        setValue(datatype, value, newMetric);

        if (timestamp !== undefined && timestamp !== null) {
            newMetric.timestamp = timestamp;
        }

        if (alias !== undefined && alias !== null) {
            newMetric.alias = alias;
        }

        if (isHistorical !== undefined && isHistorical !== null) {
            newMetric.isHistorical = isHistorical;
        }

        if (isTransient !== undefined && isTransient !== null) {
            newMetric.isTransient = isTransient;
        }

        if (value !== undefined && value === null) {
            newMetric.isNull = true;
        }

        if (metadata !== undefined && metadata !== null) {
            newMetric.metadata = encodeMetaData(metadata);
        }

        if (properties !== undefined && properties !== null) {
            newMetric.properties = encodePropertySet(properties);
        }

        return newMetric;
    }

    decodeMetric = function(protoMetric) {
        var metric = {},
            alias = protoMetric.alias,
            isHistorical = protoMetric.isHistorical,
            isTransient = protoMetric.isTransient,
            isNull = protoMetric.isNull,
            metadata = protoMetric.metadata,
            properties = protoMetric.properties,
            timestamp = protoMetric.timestamp;

        metric.name = protoMetric.name;
        metric.type = decodeType(protoMetric.datatype);
        metric.value = getValue(protoMetric.datatype, protoMetric);

        if (protoMetric.hasOwnProperty("isNull") && protoMetric.isNull === true) {
            metric.value = null;
        } else {
            metric.value = getValue(protoMetric.datatype, protoMetric);
        }

        if (protoMetric.hasOwnProperty("timestamp")) {
            metric.timestamp = protoMetric.timestamp.toNumber();
        }

        if (protoMetric.hasOwnProperty("alias")) {
            metric.alias = protoMetric.alias;
        }

        if (protoMetric.hasOwnProperty("isHistorical")) {
            metric.isHistorical = protoMetric.isHistorical;
        }

        if (protoMetric.hasOwnProperty("isTransient")) {
            metric.isTransient = protoMetric.isTransient;
        }

        if (protoMetric.hasOwnProperty("metadata")) {
            metric.metadata = decodeMetaData(protoMetric.metadata);
        }

        if (protoMetric.hasOwnProperty("properties")) {
            metric.properties = decodePropertySet(protoMetric.properties);
        }

        return metric;
    }

    exports.encodePayload = function(object) {
        var payload = Payload.create({
            "timestamp" : object.timestamp
        });

        // Build up the metric
        if (object.metrics !== undefined && object.metrics !== null) {
            var newMetrics = [],
                metrics = object.metrics;
            // loop over array of metric
            for (var i = 0; i < metrics.length; i++) {
                newMetrics.push(encodeMetric(metrics[i]));
            }
            payload.metrics = newMetrics;
        }

        if (object.seq !== undefined && object.seq !== null) {
            payload.seq = object.seq;
        }

        if (object.uuid !== undefined && object.uuid !== null) {
            payload.uuid = object.uuid;
        }

        if (object.body !== undefined && object.body !== null) {
            payload.body = object.body;
        }

        return Payload.encode(payload).finish();
    }

    exports.decodePayload = function(proto) {
        var sparkplugPayload = Payload.decode(proto),
            payload = {};

        if (sparkplugPayload.hasOwnProperty("timestamp")) {
            payload.timestamp = sparkplugPayload.timestamp.toNumber();
        }

        if (sparkplugPayload.hasOwnProperty("metrics")) {
            metrics = [];
            for (var i = 0; i < sparkplugPayload.metrics.length; i++) {
                metrics.push(decodeMetric(sparkplugPayload.metrics[i]));
            }
            payload.metrics = metrics;
        }

        if (sparkplugPayload.hasOwnProperty("seq")) {
            payload.seq = sparkplugPayload.seq.toNumber();
        }

        if (sparkplugPayload.hasOwnProperty("uuid")) {
            payload.uuid = sparkplugPayload.uuid;
        }

        if (sparkplugPayload.hasOwnProperty("body")) {
            payload.body = sparkplugPayload.body;
        }

        return payload;
    }
}());
















