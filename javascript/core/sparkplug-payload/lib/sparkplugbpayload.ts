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

import * as ProtoRoot from './sparkplugPayloadProto';
import type * as IProtoRoot from './sparkplugPayloadProto';

const Payload = ProtoRoot.org.eclipse.tahu.protobuf.Payload;
const Template = Payload.Template;
const Parameter = Template.Parameter;
const DataSet = Payload.DataSet;
const DataSetValue = DataSet.DataSetValue;
const Row = DataSet.Row;
const PropertyValue = Payload.PropertyValue;
const PropertySet = Payload.PropertySet;
const PropertySetList = Payload.PropertySetList;
const MetaData = Payload.MetaData;
const Metric = Payload.Metric;

// import generated interfaces
type IPayload = IProtoRoot.org.eclipse.tahu.protobuf.IPayload;
type ITemplate = IProtoRoot.org.eclipse.tahu.protobuf.Payload.ITemplate;
type IParameter = IProtoRoot.org.eclipse.tahu.protobuf.Payload.Template.IParameter;
type IDataSet = IProtoRoot.org.eclipse.tahu.protobuf.Payload.IDataSet;
type IDataSetValue = IProtoRoot.org.eclipse.tahu.protobuf.Payload.DataSet.IDataSetValue;
type IRow = IProtoRoot.org.eclipse.tahu.protobuf.Payload.DataSet.IRow;
type IPropertyValue = IProtoRoot.org.eclipse.tahu.protobuf.Payload.IPropertyValue;
type IPropertySet = IProtoRoot.org.eclipse.tahu.protobuf.Payload.IPropertySet;
type IPropertySetList = IProtoRoot.org.eclipse.tahu.protobuf.Payload.IPropertySetList;
type IMetaData = IProtoRoot.org.eclipse.tahu.protobuf.Payload.IMetaData;
type IMetric = IProtoRoot.org.eclipse.tahu.protobuf.Payload.IMetric;

/**
 * Sets the value of an object given it's type expressed as an integer
 */
function setValue (type, value, object) {
    switch (type) {
        case 1: // Int8
        case 2: // Int16
        case 3: // Int32
        case 5: // UInt8
        case 6: // UInt16
            object.intValue = value;
            break;
        case 4: // Int64
        case 7: // UInt32
        case 8: // UInt64
        case 13: // DateTime
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

function getValue (type, object) {
    switch (type) {
        case 1: // Int8
        case 2: // Int16
        case 3: // Int32
            return new Int32Array([object.intValue])[0];
        case 5: // UInt8
        case 6: // UInt16
            return object.intValue;
        case 4: // Int64
            return object.longValue.toSigned();
        case 7: // UInt32
            return object.longValue.toInt();
        case 8: // UInt64
        case 13: // DateTime
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

function encodeType (typeString) {
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

function decodeType (typeInt) {
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
        case 13:
            return "DateTime";
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

function encodeTypes (typeArray) {
    var types = [];
    for (var i = 0; i < typeArray.length; i++) {
        types.push(encodeType(typeArray[i]));
    }
    return types;
}

function decodeTypes (typeArray) {
    var types = [];
    for (var i = 0; i < typeArray.length; i++) {
        types.push(decodeType(typeArray[i]));
    }
    return types;
}

function encodeDataSet (object) {
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
        var newRow = Row.create(),
            row = rows[i],
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

function decodeDataSet (protoDataSet) {
    var dataSet: IDataSet = {},
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

function encodeMetaData (object) {
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

function decodeMetaData (protoMetaData) {
    var metadata: IMetaData = {},
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

function encodePropertyValue (object) {
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

function decodePropertyValue (protoValue) {
    // TODO better type
    var propertyValue: any = {};

    if (protoValue.isNull !== undefined && protoValue.isNull === true) {
        propertyValue.value = null;
    } else {
        propertyValue.value = getValue(protoValue.type, protoValue);
    }

    propertyValue.type = decodeType(protoValue.type);

    return propertyValue;
}

function encodePropertySet (object) {
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

function decodePropertySet (protoSet) {
    var propertySet = {},
        protoKeys = protoSet.keys,
        protoValues = protoSet.values;

    for (var i = 0; i < protoKeys.length; i++) {
        propertySet[protoKeys[i]] = decodePropertyValue(protoValues[i]);
    }

    return propertySet;
}

function encodePropertySetList (object) {
    var propertySets = [];
    for (var i = 0; i < object.length; i++) {
        propertySets.push(encodePropertySet(object[i]));
    }
    return PropertySetList.create({
        "propertyset" : propertySets
    });
}

function decodePropertySetList (protoSetList) {
    var propertySets = [],
        protoSets = protoSetList.propertyset;
    for (var i = 0; i < protoSets.length; i++) {
        propertySets.push(decodePropertySet(protoSets[i]));
    }
    return propertySets;
}

function encodeParameter (object) {
    var type = encodeType(object.type),
        newParameter = Parameter.create({
            "name" : object.name, 
            "type" : type
        });
    setValue(type, object.value, newParameter);
    return newParameter;
}

function decodeParameter (protoParameter) {
    var protoType = protoParameter.type,
        parameter: any = {}; // TODO better type

    parameter.name = protoParameter.name;
    parameter.type = decodeType(protoType);
    parameter.value = getValue(protoType, protoParameter);

    return parameter;
}

function encodeTemplate (object) {
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

function decodeTemplate (protoTemplate) {
    var template: ITemplate = {},
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

function encodeMetric (metric) {
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

function decodeMetric (protoMetric) {
    var metric: any = {}, // TODO better type
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
        metric.timestamp = Number(protoMetric.timestamp);
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

export function encodePayload(object) {
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

export function decodePayload(proto) {
    var sparkplugPayload = Payload.decode(proto),
        payload: IPayload = {};

    if (sparkplugPayload.hasOwnProperty("timestamp")) {
        payload.timestamp = Number(sparkplugPayload.timestamp);
    }

    if (sparkplugPayload.hasOwnProperty("metrics")) {
        const metrics: IMetric[] = [];
        for (var i = 0; i < sparkplugPayload.metrics.length; i++) {
            metrics.push(decodeMetric(sparkplugPayload.metrics[i]));
        }
        payload.metrics = metrics;
    }

    if (sparkplugPayload.hasOwnProperty("seq")) {
        payload.seq = Number(sparkplugPayload.seq);
    }

    if (sparkplugPayload.hasOwnProperty("uuid")) {
        payload.uuid = sparkplugPayload.uuid;
    }

    if (sparkplugPayload.hasOwnProperty("body")) {
        payload.body = sparkplugPayload.body;
    }

    return payload;
}
