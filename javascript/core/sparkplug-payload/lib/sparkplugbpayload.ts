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
import Long from 'long';
import type * as IProtoRoot from './sparkplugPayloadProto';
import type { Reader } from 'protobufjs';

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

// "user types"
export type TypeStr = "Int8"
    | "Int16"
    | "Int32"
    | "Int64"
    | "UInt8"
    | "UInt16"
    | "UInt32"
    | "UInt64"
    | "Float"
    | "Double"
    | "Boolean"
    | "String"
    | "DateTime"
    | "Text"
    | "UUID"
    | "DataSet"
    | "Bytes"
    | "File"
    | "Template"
    | "PropertySet"
    | "PropertySetList";

export interface UMetric extends IMetric {
    value: null | number | Long.Long | boolean | string | Uint8Array | UDataSet | UTemplate;
    type: TypeStr;
    properties?: Record<string, UPropertyValue>
}
export interface UPropertyValue extends Omit<IPropertyValue, 'type'> { // TODO is the type supposed to be like the metric type in the readme?
    value: null | number | Long.Long | boolean | string | UPropertySet | UPropertySetList;
    type: TypeStr;
}
export interface UParameter extends Omit<IParameter, 'type'> { // TODO is the type supposed to be like the metric type in the readme?
    value: number | Long.Long | boolean | string | UPropertySet | UPropertySetList;
    type: TypeStr;
}
export interface UTemplate extends Omit<ITemplate, 'metrics' | 'parameters'> { // TODO is the type supposed to be like the metric type in the readme?
    metrics?: UMetric[];
    parameters?: UParameter[];
}
export interface UDataSet extends Omit<IDataSet, 'types' | 'rows'> {
    types: TypeStr[];
    rows: UDataSetValue[][];
}
export type UDataSetValue = number | Long.Long | boolean | string;
export type UPropertySet = Record<string, UPropertyValue>;
export type UPropertySetList = UPropertySet[];
export type UserValue = UMetric['value'] | UPropertyValue['value'] | UDataSet | UDataSetValue | UPropertySet | UPropertySetList;
export interface UPayload extends IPayload {
    metrics?: UMetric[] | null;
}

/**
 * Sets the value of an object given it's type expressed as an integer
 * 
 * only used during encode functions
 */
function setValue (type: number, value: UserValue, object: IMetric | IPropertyValue) {
    // TODO not sure about type casts
    switch (type) {
        case 1: // Int8
        case 2: // Int16
        case 3: // Int32
        case 5: // UInt8
        case 6: // UInt16
            object.intValue = value as number;
            break;
        case 4: // Int64
        case 7: // UInt32
        case 8: // UInt64
        case 13: // DateTime
            object.longValue = value as number | Long;
            break;
        case 9: // Float
            object.floatValue = value as number;
            break;
        case 10: // Double
            object.doubleValue = value as number;
            break;
        case 11: // Boolean
            object.booleanValue = value as boolean;
            break;
        case 12: // String
        case 14: // Text
        case 15: // UUID
            object.stringValue = value as string;
            break;
        case 16: // DataSet
            (object as IMetric).datasetValue = encodeDataSet(value as UDataSet);
            break;
        case 17: // Bytes
        case 18: // File
            (object as IMetric).bytesValue = value as Uint8Array;
            break;
        case 19: // Template
            (object as IMetric).templateValue = encodeTemplate(value as UTemplate);
            break;
        case 20: // PropertySet
            (object as IPropertyValue).propertysetValue = encodePropertySet(value as UPropertySet);
            break;
        case 21:
            (object as IPropertyValue).propertysetsValue = encodePropertySetList(value as UPropertySetList);
            break;
    } 
}

/** only used during decode functions */
function getValue<T extends UserValue> (type: number | null | undefined, object: IMetric | IPropertyValue): T | undefined | null {
    // TODO change type casts
    switch (type) {
        case 1: // Int8
        case 2: // Int16
        case 3: // Int32
            return new Int32Array([object.intValue!])[0] as T;
        case 5: // UInt8
        case 6: // UInt16
            return object.intValue as T;
        case 4: // Int64
            if (object.longValue instanceof Long) {
                return object.longValue.toSigned() as T;
            } else {
                return object.longValue as T;
            }
        case 7: // UInt32
            if (object.longValue instanceof Long) {
                return object.longValue.toInt() as T;
            } else {
                return object.longValue as T;
            }
        case 8: // UInt64
        case 13: // DateTime
            return object.longValue! as T;
        case 9: // Float
            return object.floatValue! as T;
        case 10: // Double
            return object.doubleValue! as T;
        case 11: // Boolean
            return object.booleanValue! as T;
        case 12: // String
        case 14: // Text
        case 15: // UUID
            return object.stringValue! as T;
        case 16: // DataSet
            return decodeDataSet((object as IMetric).datasetValue!) as T;
        case 17: // Bytes
        case 18: // File
            return (object as IMetric).bytesValue as T;
        case 19: // Template
            return decodeTemplate((object as IMetric).templateValue!) as T;
        case 20: // PropertySet
            return decodePropertySet((object as IPropertyValue).propertysetValue!) as T;
        case 21:
            return decodePropertySetList((object as IPropertyValue).propertysetsValue!) as T;
        default:
            return null;
    } 
}

function isSet<T> (value: T): value is Exclude<T, null | undefined> {
    return value !== null && value !== undefined;
}

function getDataSetValue (type: number | null | undefined, object: IDataSetValue): UDataSetValue {
    switch (type) {
        case 7: // UInt32
            if (object.longValue instanceof Long) return object.longValue.toInt();
            else if (isSet(object.longValue)) return object.longValue;
        case 4: // UInt64
            if (isSet(object.longValue)) return object.longValue;
        case 9: // Float
            if (isSet(object.floatValue)) return object.floatValue;
        case 10: // Double
            if (isSet(object.doubleValue)) return object.doubleValue;
        case 11: // Boolean
            if (isSet(object.booleanValue)) return object.booleanValue;
        case 12: // String
            if (isSet(object.stringValue)) return object.stringValue;
        default:
            throw new Error(`Invalid DataSetValue: ${JSON.stringify(object)}`);
    }
}

function getTemplateParamValue (type: number | null | undefined, object: IParameter): UParameter['value'] {
    switch (type) {
        case 7: // UInt32
            if (object.longValue instanceof Long) return object.longValue.toInt();
            else if (isSet(object.longValue)) return object.longValue;
        case 4: // UInt64
            if (isSet(object.longValue)) return object.longValue;
        case 9: // Float
            if (isSet(object.floatValue)) return object.floatValue;
        case 10: // Double
            if (isSet(object.doubleValue)) return object.doubleValue;
        case 11: // Boolean
            if (isSet(object.booleanValue)) return object.booleanValue;
        case 12: // String
            if (isSet(object.stringValue)) return object.stringValue;
        default:
            throw new Error(`Invalid Parameter value: ${JSON.stringify(object)}`);
    }
}

/** transforms a user friendly type and converts it to its corresponding type code */
function encodeType (typeString: string): number {
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

/** transforms a type code into a user friendly type */
// @ts-expect-error TODO no consistent return
function decodeType (typeInt: number | null | undefined): TypeStr {
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

function encodeTypes (typeArray: string[]): number[]  {
    var types: number[] = [];
    for (var i = 0; i < typeArray.length; i++) {
        types.push(encodeType(typeArray[i]));
    }
    return types;
}

function decodeTypes (typeArray: number[]): TypeStr[] {
    var types: TypeStr[] = [];
    for (var i = 0; i < typeArray.length; i++) {
        types.push(decodeType(typeArray[i]));
    }
    return types;
}

function encodeDataSet (object: UDataSet): ProtoRoot.org.eclipse.tahu.protobuf.Payload.DataSet {
    const num = object.numOfColumns,
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
    for (let i = 0; i < rows.length; i++) {
        const newRow = Row.create(),
            row = rows[i],
            elements: IDataSetValue[] = [];
        // Loop over all the elements in each row
        // @ts-expect-error TODO check if num is set
        for (let t = 0; t < num; t++) {
            const newValue = DataSetValue.create();
            setValue(types[t], row[t], newValue);
            elements.push(newValue);
        }
        newRow.elements = elements;
        newRows.push(newRow);
    }
    newDataSet.rows = newRows;
    return newDataSet;
}

function decodeDataSet (protoDataSet: IDataSet): UDataSet {
    const protoTypes = protoDataSet.types!; // TODO check exists
    const dataSet: UDataSet = {
        types: decodeTypes(protoTypes),
        rows: [],
    };
    const types = decodeTypes(protoTypes),
        protoRows = protoDataSet.rows || [], // TODO check exists
        num = protoDataSet.numOfColumns;
    
    // Loop over all the rows
    for (var i = 0; i < protoRows.length; i++) {
        var protoRow = protoRows[i],
            protoElements = protoRow.elements || [], // TODO check exists
            rowElements: UDataSetValue[] = [];
        // Loop over all the elements in each row
        // @ts-expect-error TODO check exists
        for (var t = 0; t < num; t++) {
            rowElements.push(getDataSetValue(protoTypes[t], protoElements[t])!);
        }
        dataSet.rows.push(rowElements);
    }

    dataSet.numOfColumns = num;
    dataSet.types = types;
    dataSet.columns = protoDataSet.columns;

    return dataSet;
}

function encodeMetaData (object: IMetaData): ProtoRoot.org.eclipse.tahu.protobuf.Payload.MetaData {
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

function decodeMetaData (protoMetaData: IMetaData): IMetaData {
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

function encodePropertyValue (object: UPropertyValue): ProtoRoot.org.eclipse.tahu.protobuf.Payload.PropertyValue {
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

function decodePropertyValue (protoValue: IPropertyValue): UPropertyValue {
    const propertyValue: UPropertyValue = {
        // @ts-expect-error TODO check exists
        value: getValue(protoValue.type, protoValue),
        type: decodeType(protoValue.type),
    };

    if (protoValue.isNull !== undefined && protoValue.isNull === true) {
        propertyValue.value = null;
    } else {
        propertyValue.value = getValue(protoValue.type, protoValue)!;
    }

    propertyValue.type = decodeType(protoValue.type);

    return propertyValue;
}

function encodePropertySet (object: Record<string, UPropertyValue>): ProtoRoot.org.eclipse.tahu.protobuf.Payload.PropertySet {
    const keys = [],
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

function decodePropertySet (protoSet: IPropertySet): Record<string, UPropertyValue> {
    const propertySet: Record<string, UPropertyValue> = {},
        protoKeys = protoSet.keys || [], // TODO check exists
        protoValues = protoSet.values || []; // TODO check exists

    for (var i = 0; i < protoKeys.length; i++) {
        propertySet[protoKeys[i]] = decodePropertyValue(protoValues[i]);
    }

    return propertySet;
}

function encodePropertySetList (object: Record<string, UPropertyValue>[]): ProtoRoot.org.eclipse.tahu.protobuf.Payload.PropertySetList {
    const propertySets: IPropertySet[] = [];
    for (let i = 0; i < object.length; i++) {
        propertySets.push(encodePropertySet(object[i]));
    }
    return PropertySetList.create({
        "propertyset" : propertySets
    });
}

function decodePropertySetList (protoSetList: IPropertySetList): Record<string, UPropertyValue>[]  {
    const propertySets: Record<string, UPropertyValue>[] = [],
        protoSets = protoSetList.propertyset || []; // TODO check exists
    for (let i = 0; i < protoSets.length; i++) {
        propertySets.push(decodePropertySet(protoSets[i]));
    }
    return propertySets;
}

function encodeParameter (object: UParameter): ProtoRoot.org.eclipse.tahu.protobuf.Payload.Template.Parameter {
    const type = encodeType(object.type),
        newParameter = Parameter.create({
            "name" : object.name, 
            "type" : type
        });
    setValue(type, object.value, newParameter);
    return newParameter;
}

function decodeParameter (protoParameter: IParameter): UParameter {
    const protoType = protoParameter.type,
        parameter: UParameter = {
            value: getTemplateParamValue(protoType, protoParameter),
            type: decodeType(protoType),
        };

    parameter.name = protoParameter.name;
    parameter.type = decodeType(protoType);
    // @ts-expect-error TODO check exists
    parameter.value = getValue(protoType, protoParameter);

    return parameter;
}

function encodeTemplate (object: UTemplate): ProtoRoot.org.eclipse.tahu.protobuf.Payload.Template {
    let template = Template.create(),
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
        const newMetrics = []
            metrics = object.metrics;
        // loop over array of metrics
        for (let i = 0; i < metrics.length; i++) {
            newMetrics.push(encodeMetric(metrics[i]));
        }
        template.metrics = newMetrics;
    }

    // Build up the parameters
    if (object.parameters !== undefined && object.parameters !== null) {
        const newParameter = [];
        // loop over array of parameters
        for (let i = 0; i < object.parameters.length; i++) {
            newParameter.push(encodeParameter(object.parameters[i]));
        }
        template.parameters = newParameter;
    }

    return template;
}

function decodeTemplate (protoTemplate: ITemplate): UTemplate {
    const template: UTemplate = {},
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
        const metrics = []
        // loop over array of proto metrics, decoding each one
        for (let i = 0; i < protoMetrics.length; i++) {
            metrics.push(decodeMetric(protoMetrics[i]));
        }
        template.metrics = metrics;
    }

    // Build up the parameters
    if (protoParameters !== undefined && protoParameters !== null) {
        const parameter: UParameter[] = [];
        // loop over array of parameters
        for (let i = 0; i < protoParameters.length; i++) {
            parameter.push(decodeParameter(protoParameters[i]));
        }
        template.parameters = parameter;
    }

    return template;
}

function encodeMetric (metric: UMetric): ProtoRoot.org.eclipse.tahu.protobuf.Payload.Metric {
    const newMetric = Metric.create({
            name : metric.name
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

function decodeMetric (protoMetric: Partial<IMetric>): UMetric {
    const metric: UMetric = {
        // @ts-expect-error TODO check exists
        value: getValue(protoMetric.datatype, protoMetric),
        type: decodeType(protoMetric.datatype)
    };

    if (protoMetric.hasOwnProperty("name")) {
        metric.name = protoMetric.name;
    }

    if (protoMetric.hasOwnProperty("isNull") && protoMetric.isNull === true) {
        metric.value = null;
    } else {
        // @ts-expect-error TODO check exists
        metric.value = getValue(protoMetric.datatype, protoMetric);
    }

    if (protoMetric.hasOwnProperty("timestamp")) {
        metric.timestamp = protoMetric.timestamp;
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

    if (protoMetric.hasOwnProperty("metadata") && protoMetric.metadata) {
        metric.metadata = decodeMetaData(protoMetric.metadata);
    }

    if (protoMetric.hasOwnProperty("properties") && protoMetric.properties) {
        metric.properties = decodePropertySet(protoMetric.properties);
    }

    return metric;
}

export function encodePayload(object: UPayload): Uint8Array {
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

export function decodePayload(proto: Uint8Array | Reader): UPayload {
    var sparkplugPayload = Payload.decode(proto),
        payload: UPayload = {};

    if (sparkplugPayload.hasOwnProperty("timestamp")) {
        payload.timestamp = sparkplugPayload.timestamp;
    }

    if (sparkplugPayload.hasOwnProperty("metrics")) {
        const metrics: UMetric[] = [];
        for (var i = 0; i < sparkplugPayload.metrics.length; i++) {
            metrics.push(decodeMetric(sparkplugPayload.metrics[i]));
        }
        payload.metrics = metrics;
    }

    if (sparkplugPayload.hasOwnProperty("seq")) {
        payload.seq = sparkplugPayload.seq;
    }

    if (sparkplugPayload.hasOwnProperty("uuid")) {
        payload.uuid = sparkplugPayload.uuid;
    }

    if (sparkplugPayload.hasOwnProperty("body")) {
        payload.body = sparkplugPayload.body;
    }

    return payload;
}
