/********************************************************************************
 * Copyright (c) 2014-2019 Cirrus Link Solutions and others
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

#include <stdio.h>
#include <stdlib.h>
#include <stdbool.h>
#include <pb_decode.h>
#include <pb_encode.h>
#include <tahu.h>
#include <tahu.pb.h>

// Global sequence variable
uint64_t seq;

/*
 * Private function to decode a Metric from a stream
 */
bool decode_metric(org_eclipse_tahu_protobuf_Payload_Metric *metric, pb_istream_t *stream) {
	bool status;
	pb_istream_t substream;

	if (!pb_make_string_substream(stream, &substream)) {
		fprintf(stderr, "Failed to create the substream for metric decoding\n");
		return false;
	}

	pb_wire_type_t metric_wire_type;
	uint32_t metric_tag;
	bool metric_eof;
	const pb_field_t *metric_field;

	while (pb_decode_tag(&substream, &metric_wire_type, &metric_tag, &metric_eof)) {
		DEBUG_PRINT(("\teof: %s\n", metric_eof ? "true" : "false"));
		DEBUG_PRINT(("\t\tBytes Remaining: %zd\n", substream.bytes_left));
		DEBUG_PRINT(("\t\tWiretype: %d\n", metric_wire_type));
		DEBUG_PRINT(("\t\tTag: %d\n", metric_tag));

		if (metric_wire_type == PB_WT_VARINT) {
			DEBUG_PRINT(("\t\tMetric Wire type is PB_WT_VARINT\n"));
			for (metric_field = org_eclipse_tahu_protobuf_Payload_Metric_fields; metric_field->tag != 0; metric_field++) {
				if (metric_field->tag == metric_tag && (((metric_field->type & PB_LTYPE_VARINT) == PB_LTYPE_VARINT) ||
													((metric_field->type & PB_LTYPE_UVARINT) == PB_LTYPE_UVARINT))) {
					DEBUG_PRINT(("\t\tWire type is PB_WT_VARINT\n"));
					uint64_t dest;
					status = pb_decode_varint(&substream, &dest);
					if (status) {
						DEBUG_PRINT(("\t\tVARINT - Success - new value: %ld\n", dest));
						if (metric_field->tag == org_eclipse_tahu_protobuf_Payload_Metric_alias_tag) {
							metric->has_alias = true;
							metric->alias = dest;
						} else if (metric_field->tag == org_eclipse_tahu_protobuf_Payload_Metric_timestamp_tag) {
							metric->has_timestamp = true;
							metric->timestamp = dest;
						} else if (metric_field->tag == org_eclipse_tahu_protobuf_Payload_Metric_datatype_tag) {
							metric->has_datatype = true;
							metric->datatype = dest;
						} else if (metric_field->tag == org_eclipse_tahu_protobuf_Payload_Metric_is_historical_tag) {
							metric->has_is_historical = true;
							metric->is_historical = dest;
						} else if (metric_field->tag == org_eclipse_tahu_protobuf_Payload_Metric_is_transient_tag) {
							metric->has_is_transient = true;
							metric->is_transient = dest;
						} else if (metric_field->tag == org_eclipse_tahu_protobuf_Payload_Metric_is_null_tag) {
							metric->has_is_null = true;
							metric->is_null = dest;
						} else if (metric_field->tag == org_eclipse_tahu_protobuf_Payload_Metric_int_value_tag) {
							metric->which_value = org_eclipse_tahu_protobuf_Payload_Metric_int_value_tag;
							metric->value.int_value = dest;
						} else if (metric_field->tag == org_eclipse_tahu_protobuf_Payload_Metric_long_value_tag) {
							metric->which_value = org_eclipse_tahu_protobuf_Payload_Metric_long_value_tag;
							metric->value.long_value = dest;
						} else if (metric_field->tag == org_eclipse_tahu_protobuf_Payload_Metric_boolean_value_tag) {
							metric->which_value = org_eclipse_tahu_protobuf_Payload_Metric_boolean_value_tag;
							metric->value.boolean_value = dest;
						}
					} else {
						fprintf(stderr, "\t\tVARINT - Failed to decode variant!\n");
						return false;
					}
				} else if (metric_field->tag == metric_tag && ((metric_field->type & PB_LTYPE_SVARINT) == PB_LTYPE_SVARINT)) {
					DEBUG_PRINT(("\t\tWire type is PB_WT_SVARINT\n"));
					int64_t dest;
					status = pb_decode_svarint(&substream, &dest);
					if (status) {
						DEBUG_PRINT(("\t\tVARINT - Success - new value: %ld\n", dest));
					} else {
						fprintf(stderr, "\t\tVARINT - Failed to decode variant!\n");
						return false;
					}
				}
			}
		} else if (metric_wire_type == PB_WT_32BIT) {
			DEBUG_PRINT(("\t\tMetric Wire type is PB_WT_32BIT\n"));
			for (metric_field = org_eclipse_tahu_protobuf_Payload_Metric_fields; metric_field->tag != 0; metric_field++) {
				if (metric_field->tag == metric_tag && (((metric_field->type & PB_LTYPE_FIXED32) == PB_LTYPE_FIXED32))) {
					DEBUG_PRINT(("\t\tWire type is PB_WT_32BIT\n"));
					uint32_t dest;
					status = pb_decode_fixed32(&substream, &dest);
					if (status) {
						DEBUG_PRINT(("\t\t32BIT - Success - new value: %d\n", dest));
						float destination_float = *((float*)&dest);
						DEBUG_PRINT(("\t\tFLoat - Success - new value: %f\n", destination_float));
						if (metric_field->tag == org_eclipse_tahu_protobuf_Payload_Metric_float_value_tag) {
							metric->which_value = org_eclipse_tahu_protobuf_Payload_Metric_float_value_tag;
							metric->value.float_value = destination_float;
						}
					}
				}
			}
		} else if (metric_wire_type == PB_WT_64BIT) {
			DEBUG_PRINT(("\t\tMetric Wire type is PB_WT_64BIT\n"));
			for (metric_field = org_eclipse_tahu_protobuf_Payload_Metric_fields; metric_field->tag != 0; metric_field++) {
				if (metric_field->tag == metric_tag && (((metric_field->type & PB_LTYPE_FIXED64) == PB_LTYPE_FIXED64))) {
					DEBUG_PRINT(("\t\tWire type is PB_WT_64BIT\n"));
					uint64_t dest;
					status = pb_decode_fixed64(&substream, &dest);
					if (status) {
						DEBUG_PRINT(("\t\t64BIT - Success - new value: %ld\n", dest));
						double destination_double = *((double*)&dest);
						DEBUG_PRINT(("\t\tDouble - Success - new value: %f\n", destination_double));
						if (metric_field->tag == org_eclipse_tahu_protobuf_Payload_Metric_double_value_tag) {
							metric->which_value = org_eclipse_tahu_protobuf_Payload_Metric_double_value_tag;
							metric->value.double_value = destination_double;
						}
					}
				}
			}

		} else if (metric_wire_type == PB_WT_STRING) {
			DEBUG_PRINT(("\t\tMetric Wire type is PB_WT_STRING\n"));

			for (metric_field = org_eclipse_tahu_protobuf_Payload_Metric_fields; metric_field->tag != 0; metric_field++) {
				if (metric_field->tag == metric_tag && ((metric_field->type & PB_LTYPE_SUBMESSAGE) == PB_LTYPE_SUBMESSAGE)) {
					DEBUG_PRINT(("\t\tFound a PB_LTYPE_SUBMESSAGE\n"));
				} else if (metric_field->tag == metric_tag &&
							((metric_field->type & PB_LTYPE_FIXED_LENGTH_BYTES) == PB_LTYPE_FIXED_LENGTH_BYTES)) {
					DEBUG_PRINT(("\t\tFound a PB_LTYPE_FIXED_LENGTH_BYTES\n"));
				} else if (metric_field->tag == metric_tag && ((metric_field->type & PB_LTYPE_STRING) == PB_LTYPE_STRING)) {
					DEBUG_PRINT(("\t\tFound a PB_LTYPE_STRING\n"));

					// Get the string size
					pb_byte_t string_size[1];
					status = pb_read(&substream, string_size, 1);
					if (status) {
						DEBUG_PRINT(("\t\tString Size: %d\n", string_size[0]));
					} else {
						fprintf(stderr, "\t\tFailed to get the string size while decoding\n");
						return false;
					}

					pb_byte_t dest[string_size[0]+1];
					status = pb_read(&substream, dest, string_size[0]);
					if (status) {
						dest[string_size[0]] = '\0';

						// This is either the metric name or string value
						if (metric_field->tag == org_eclipse_tahu_protobuf_Payload_Metric_name_tag) {
							DEBUG_PRINT(("\t\tRead the Metric name! %s\n", dest));
							metric->name = (char *)malloc((strlen(dest)+1)*sizeof(char));
							strcpy(metric->name, dest);
						} else if (metric_field->tag == org_eclipse_tahu_protobuf_Payload_Metric_string_value_tag) {
							DEBUG_PRINT(("\t\tRead the Metric string_value! %s\n", dest));
							metric->which_value = org_eclipse_tahu_protobuf_Payload_Metric_string_value_tag;
							// JPL 04/05/17... I hope this gets FREE(string_value)'d somewhere
							metric->value.string_value =(char *)malloc((strlen(dest)+1)*sizeof(char));
							strcpy(metric->value.string_value, dest );
							// JPL 04/05/17... local memory?
							//	metric->value.string_value = dest;
						}
					} else {
						fprintf(stderr, "\t\tFailed to read the string...\n");
						return false;
					}
				} else if (metric_field->tag == metric_tag && ((metric_field->type & PB_LTYPE_BYTES) == PB_LTYPE_BYTES)) {
					DEBUG_PRINT(("\t\tFound a PB_LTYPE_BYTES\n"));
				//} else {
					//DEBUG_PRINT(("\t\tother: %d\n", metric_field->type);
				}
			}

		} else if (metric_wire_type == PB_WT_32BIT) {
			DEBUG_PRINT(("\t\tMetric Wire type is PB_WT_32BIT\n"));
		//} else {
			//DEBUG_PRINT(("\t\tMetric Other? %d\n", metric_wire_type);
		}
	}

	// Close the substream
	pb_close_string_substream(stream, &substream);
}

/*
 * Private function to increase the size of an array of strings
 */
int grow_char_array(char **array, int current_size, int num_new_elems) {
        const int total_size = current_size + num_new_elems;
	char *temp = (char *)realloc(*array, (total_size * sizeof(char*)));

	if (temp == NULL) {
		fprintf(stderr, "Cannot allocate more memory.\n");
		return 0;
	} else {
		*array = temp;
	}

	return total_size;
}

/*
 * Private function to increase the size of an array of Metrics
 */
int grow_metrics_array(org_eclipse_tahu_protobuf_Payload_Metric **metric_array, int current_size, int num_new_elems) {
        const int total_size = current_size + num_new_elems;
        org_eclipse_tahu_protobuf_Payload_Metric *temp = (org_eclipse_tahu_protobuf_Payload_Metric*)realloc(*metric_array,
                                                                 	(total_size * sizeof(org_eclipse_tahu_protobuf_Payload_Metric)));

	if (temp == NULL) {
		fprintf(stderr, "Cannot allocate more memory.\n");
		return 0;
	} else {
		*metric_array = temp;
	}

        return total_size;
}

/*
 * Private function to increase the size of an array of PropertyValues
 */
int grow_propertyvalues_array(org_eclipse_tahu_protobuf_Payload_PropertyValue **values_array, int current_size, int num_new_elems) {
        const int total_size = current_size + num_new_elems;
        org_eclipse_tahu_protobuf_Payload_PropertyValue *temp = (org_eclipse_tahu_protobuf_Payload_PropertyValue*)realloc(*values_array,
                                                                 (total_size * sizeof(org_eclipse_tahu_protobuf_Payload_PropertyValue)));

	if (temp == NULL) {
		fprintf(stderr, "Cannot allocate more memory.\n");
		return 0;
	} else {
		*values_array = temp;
	}

        return total_size;
}

/*
 * Add Metadata to an existing Metric
 */
void add_metadata_to_metric(org_eclipse_tahu_protobuf_Payload_Metric *metric, org_eclipse_tahu_protobuf_Payload_MetaData *metadata) {
	metric->has_metadata = true;
	metric->metadata = *metadata;
}

/*
 * Add a complete Metric to an existing Payload
 */
void add_metric_to_payload(org_eclipse_tahu_protobuf_Payload *payload, org_eclipse_tahu_protobuf_Payload_Metric *metric) {

	int size = payload->metrics_count;
	if (size == 0) {
		payload->metrics = (org_eclipse_tahu_protobuf_Payload_Metric *) calloc(1, sizeof(org_eclipse_tahu_protobuf_Payload_Metric));
		if(payload->metrics == NULL) {
			fprintf(stderr, "Cannot allocate initial memory for data\n");
		} else {
			size = 1;
		}
	} else {
		size = grow_metrics_array(&payload->metrics, size, 1);
	}

	// Assign the metric
	payload->metrics[payload->metrics_count] = *metric;

	// Increment the metric count
	payload->metrics_count++;
}

/*
 * Add a simple Property to an existing PropertySet
 */
bool add_property_to_set(org_eclipse_tahu_protobuf_Payload_PropertySet *propertyset, const char *key, uint32_t datatype, bool is_null, const void *value, size_t size_of_value) {

	if(propertyset->keys_count != propertyset->values_count) {
		fprintf(stderr, "Invalid PropertySet!\n");
		return false;
	}

	int size = propertyset->keys_count;
	if (size == 0) {
		propertyset->keys = (char **) calloc(1, sizeof(char*));
		propertyset->values = (org_eclipse_tahu_protobuf_Payload_PropertyValue *) calloc(1, sizeof(org_eclipse_tahu_protobuf_Payload_PropertyValue));
		if(propertyset->values == NULL) {
			fprintf(stderr, "Cannot allocate initial memory for data\n");
		} else {
			size = 1;
		}
	} else {
		grow_char_array(propertyset->keys, size, 1);
		size = grow_propertyvalues_array(&propertyset->values, size, 1);
	}

	// Set the key name in the array of keys
	propertyset->keys[size-1] = (char *)malloc((strlen(key)+1)*sizeof(char));
	strcpy(propertyset->keys[size-1], key);

	// Set the value components
	propertyset->values[size-1].has_type = true;
	propertyset->values[size-1].type = datatype;
	propertyset->values[size-1].has_is_null = is_null;
	if (is_null) {
		propertyset->values[size-1].is_null = true;
	}
	if (datatype == PROPERTY_DATA_TYPE_UNKNOWN) {
		fprintf(stderr, "Can't create property value with unknown datatype!\n");
	} else if (datatype == PROPERTY_DATA_TYPE_INT8) {
		DEBUG_PRINT(("Setting datatype: %d, with value: %d\n", datatype, *((int8_t *)value)));
		propertyset->values[size-1].which_value = org_eclipse_tahu_protobuf_Payload_PropertyValue_int_value_tag;
		propertyset->values[size-1].value.int_value = *((int8_t *)value);
	} else if (datatype == PROPERTY_DATA_TYPE_INT16) {
		DEBUG_PRINT(("Setting datatype: %d, with value: %d\n", datatype, *((int16_t *)value)));
		propertyset->values[size-1].which_value = org_eclipse_tahu_protobuf_Payload_PropertyValue_int_value_tag;
		propertyset->values[size-1].value.int_value = *((int16_t *)value);
	} else if (datatype == PROPERTY_DATA_TYPE_INT32) {
		DEBUG_PRINT(("Setting datatype: %d, with value: %d\n", datatype, *((int32_t *)value)));
		propertyset->values[size-1].which_value = org_eclipse_tahu_protobuf_Payload_PropertyValue_int_value_tag;
		propertyset->values[size-1].value.int_value = *((int32_t *)value);
	} else if (datatype == PROPERTY_DATA_TYPE_INT64) {
		DEBUG_PRINT(("Setting datatype: %d, with value: %zd\n", datatype, *((int64_t *)value)));
		propertyset->values[size-1].which_value = org_eclipse_tahu_protobuf_Payload_PropertyValue_long_value_tag;
		propertyset->values[size-1].value.long_value = *((int64_t *)value);
	} else if (datatype == PROPERTY_DATA_TYPE_UINT8) {
		DEBUG_PRINT(("Setting datatype: %d, with value: %u\n", datatype, *((uint8_t *)value)));
		propertyset->values[size-1].which_value = org_eclipse_tahu_protobuf_Payload_PropertyValue_int_value_tag;
		propertyset->values[size-1].value.int_value = *((uint8_t *)value);
	} else if (datatype == PROPERTY_DATA_TYPE_UINT16) {
		DEBUG_PRINT(("Setting datatype: %d, with value: %u\n", datatype, *((uint16_t *)value)));
		propertyset->values[size-1].which_value = org_eclipse_tahu_protobuf_Payload_PropertyValue_int_value_tag;
		propertyset->values[size-1].value.int_value = *((uint16_t *)value);
	} else if (datatype == PROPERTY_DATA_TYPE_UINT32) {
		DEBUG_PRINT(("Setting datatype: %d, with value: %u\n", datatype, *((uint32_t *)value)));
		propertyset->values[size-1].which_value = org_eclipse_tahu_protobuf_Payload_PropertyValue_long_value_tag;
		propertyset->values[size-1].value.long_value = *((uint32_t *)value);
	} else if (datatype == PROPERTY_DATA_TYPE_UINT64) {
		DEBUG_PRINT(("Setting datatype: %d, with value: %zd\n", datatype, *((uint64_t *)value)));
		propertyset->values[size-1].which_value = org_eclipse_tahu_protobuf_Payload_PropertyValue_long_value_tag;
		propertyset->values[size-1].value.long_value = *((uint64_t *)value);
	} else if (datatype == PROPERTY_DATA_TYPE_DATETIME) {
		DEBUG_PRINT(("Setting datatype: %d, with value: %zd\n", datatype, *((uint64_t *)value)));
		propertyset->values[size-1].which_value = org_eclipse_tahu_protobuf_Payload_PropertyValue_long_value_tag;
		propertyset->values[size-1].value.long_value = *((uint64_t *)value);
	} else if (datatype == PROPERTY_DATA_TYPE_FLOAT) {
		DEBUG_PRINT(("Setting datatype: %d, with value: %f\n", datatype, *((float *)value)));
		propertyset->values[size-1].which_value = org_eclipse_tahu_protobuf_Payload_PropertyValue_float_value_tag;
		propertyset->values[size-1].value.float_value = *((float *)value);
	} else if (datatype == PROPERTY_DATA_TYPE_DOUBLE) {
		DEBUG_PRINT(("Setting datatype: %d, with value: %f\n", datatype, *((double *)value)));
		propertyset->values[size-1].which_value = org_eclipse_tahu_protobuf_Payload_PropertyValue_double_value_tag;
		propertyset->values[size-1].value.double_value = *((double *)value);
	} else if (datatype == PROPERTY_DATA_TYPE_BOOLEAN) {
		DEBUG_PRINT(("Setting datatype: %d, with value: %d\n", datatype, *((bool *)value)));
		propertyset->values[size-1].which_value = org_eclipse_tahu_protobuf_Payload_PropertyValue_boolean_value_tag;
		propertyset->values[size-1].value.boolean_value = *((bool *)value);
	} else if (datatype == PROPERTY_DATA_TYPE_STRING || datatype == PROPERTY_DATA_TYPE_TEXT) {
		DEBUG_PRINT(("Setting datatype: %d, with value: %s\n", datatype, (char *)value));
		propertyset->values[size-1].which_value = org_eclipse_tahu_protobuf_Payload_PropertyValue_string_value_tag;
		propertyset->values[size-1].value.string_value = (char *)malloc(size_of_value*sizeof(char));
		strcpy(propertyset->values[size-1].value.string_value, (char *)value);
	} else {
		fprintf(stderr, "Unknown datatype %u\n", datatype);
	}

	propertyset->keys_count++;
	propertyset->values_count++;
	DEBUG_PRINT(("Size of values in PropertySet %d\n", propertyset->keys_count));
}

/*
 * Add a PropertySet to an existing Metric
 */
void add_propertyset_to_metric(org_eclipse_tahu_protobuf_Payload_Metric *metric, org_eclipse_tahu_protobuf_Payload_PropertySet *properties) {
	metric->has_properties = true;
	metric->properties = *properties;
}

/*
 * Add a simple Metric to an existing Payload
 */
void add_simple_metric(org_eclipse_tahu_protobuf_Payload *payload,
			const char *name,
			bool has_alias,
			uint64_t alias,
			uint64_t datatype,
			bool is_historical,
			bool is_transient,
			bool is_null,
			const void *value,
			size_t size_of_value) {

	int size = payload->metrics_count;
	if (size == 0) {
		payload->metrics = (org_eclipse_tahu_protobuf_Payload_Metric *) calloc(1, sizeof(org_eclipse_tahu_protobuf_Payload_Metric));
		if(payload->metrics == NULL) {
			fprintf(stderr, "Cannot allocate initial memory for data\n");
		} else {
			size = 1;
		}
	} else {
		size = grow_metrics_array(&payload->metrics, size, 1);
	}

	if (name == NULL) {
		DEBUG_PRINT(("Name is null"));
		payload->metrics[size-1].name = NULL;
	} else {
		payload->metrics[size-1].name = (char *)malloc((strlen(name)+1)*sizeof(char));
		strcpy(payload->metrics[size-1].name, name);
	}
	payload->metrics[size-1].has_alias = has_alias;
	if (has_alias) {
		payload->metrics[size-1].alias = alias;
	}
	payload->metrics[size-1].has_timestamp = true;
	payload->metrics[size-1].timestamp = get_current_timestamp();
	payload->metrics[size-1].has_datatype = true;
	payload->metrics[size-1].datatype = datatype;
	payload->metrics[size-1].has_is_historical = is_historical;
	if (is_historical) {
		payload->metrics[size-1].is_historical = is_historical;
	}
	payload->metrics[size-1].has_is_transient = is_transient;
	if (is_transient) {
		payload->metrics[size-1].is_transient = is_transient;
	}
	payload->metrics[size-1].has_is_null = is_null;
	if (is_null) {
		payload->metrics[size-1].is_null = is_null;
	}
	payload->metrics[size-1].has_metadata = false;
	payload->metrics[size-1].has_properties = false;

	// Default dynamically allocated members to NULL
	payload->metrics[size-1].value.string_value = NULL;

	DEBUG_PRINT(("Setting datatype and value - value size is %zd\n", size_of_value));
	if (datatype == METRIC_DATA_TYPE_UNKNOWN) {
		fprintf(stderr, "Can't create metric with unknown datatype!\n");
	} else if (datatype == METRIC_DATA_TYPE_INT8) {
		DEBUG_PRINT(("Setting datatype: %zd, with value: %d\n", datatype, *((int8_t *)value)));
		payload->metrics[size-1].which_value = org_eclipse_tahu_protobuf_Payload_Metric_int_value_tag;
		payload->metrics[size-1].value.int_value = *((int8_t *)value);
	} else if (datatype == METRIC_DATA_TYPE_INT16) {
		DEBUG_PRINT(("Setting datatype: %zd, with value: %d\n", datatype, *((int16_t *)value)));
		payload->metrics[size-1].which_value = org_eclipse_tahu_protobuf_Payload_Metric_int_value_tag;
		payload->metrics[size-1].value.int_value = *((int16_t *)value);
	} else if (datatype == METRIC_DATA_TYPE_INT32) {
		DEBUG_PRINT(("Setting datatype: %zd, with value: %d\n", datatype, *((int32_t *)value)));
		payload->metrics[size-1].which_value = org_eclipse_tahu_protobuf_Payload_Metric_int_value_tag;
		payload->metrics[size-1].value.int_value = *((int32_t *)value);
	} else if (datatype == METRIC_DATA_TYPE_INT64) {
		DEBUG_PRINT(("Setting datatype: %zd, with value: %zd\n", datatype, *((int64_t *)value)));
		payload->metrics[size-1].which_value = org_eclipse_tahu_protobuf_Payload_Metric_long_value_tag;
		payload->metrics[size-1].value.long_value = *((int64_t *)value);
	} else if (datatype == METRIC_DATA_TYPE_UINT8) {
		DEBUG_PRINT(("Setting datatype: %zd, with value: %u\n", datatype, *((uint8_t *)value)));
		payload->metrics[size-1].which_value = org_eclipse_tahu_protobuf_Payload_Metric_int_value_tag;
		payload->metrics[size-1].value.int_value = *((uint8_t *)value);
	} else if (datatype == METRIC_DATA_TYPE_UINT16) {
		DEBUG_PRINT(("Setting datatype: %zd, with value: %u\n", datatype, *((uint16_t *)value)));
		payload->metrics[size-1].which_value = org_eclipse_tahu_protobuf_Payload_Metric_int_value_tag;
		payload->metrics[size-1].value.int_value = *((uint16_t *)value);
	} else if (datatype == METRIC_DATA_TYPE_UINT32) {
		DEBUG_PRINT(("Setting datatype: %zd, with value: %u\n", datatype, *((uint32_t *)value)));
		payload->metrics[size-1].which_value = org_eclipse_tahu_protobuf_Payload_Metric_long_value_tag;
		payload->metrics[size-1].value.long_value = *((uint32_t *)value);
	} else if (datatype == METRIC_DATA_TYPE_UINT64) {
		DEBUG_PRINT(("Setting datatype: %zd, with value: %zd\n", datatype, *((uint64_t *)value)));
		payload->metrics[size-1].which_value = org_eclipse_tahu_protobuf_Payload_Metric_long_value_tag;
		payload->metrics[size-1].value.long_value = *((uint64_t *)value);
	} else if (datatype == METRIC_DATA_TYPE_DATETIME) {
		DEBUG_PRINT(("Setting datatype: %zd, with value: %zd\n", datatype, *((uint64_t *)value)));
		payload->metrics[size-1].which_value = org_eclipse_tahu_protobuf_Payload_Metric_long_value_tag;
		payload->metrics[size-1].value.long_value = *((uint64_t *)value);
	} else if (datatype == METRIC_DATA_TYPE_FLOAT) {
		DEBUG_PRINT(("Setting datatype: %zd, with value: %f\n", datatype, *((float *)value)));
		payload->metrics[size-1].which_value = org_eclipse_tahu_protobuf_Payload_Metric_float_value_tag;
		payload->metrics[size-1].value.float_value = *((float *)value);
	} else if (datatype == METRIC_DATA_TYPE_DOUBLE) {
		DEBUG_PRINT(("Setting datatype: %zd, with value: %f\n", datatype, *((double *)value)));
		payload->metrics[size-1].which_value = org_eclipse_tahu_protobuf_Payload_Metric_double_value_tag;
		payload->metrics[size-1].value.double_value = *((double *)value);
	} else if (datatype == METRIC_DATA_TYPE_BOOLEAN) {
		DEBUG_PRINT(("Setting datatype: %zd, with value: %d\n", datatype, *((bool *)value)));
		payload->metrics[size-1].which_value = org_eclipse_tahu_protobuf_Payload_Metric_boolean_value_tag;
		payload->metrics[size-1].value.boolean_value = *((bool *)value);
	} else if (datatype == METRIC_DATA_TYPE_STRING || datatype == METRIC_DATA_TYPE_TEXT || datatype == METRIC_DATA_TYPE_UUID) {
		DEBUG_PRINT(("Setting datatype: %zd, with value: %s\n", datatype, (char *)value));
		payload->metrics[size-1].which_value = org_eclipse_tahu_protobuf_Payload_Metric_string_value_tag;
		payload->metrics[size-1].value.string_value = (char *)malloc(size_of_value*sizeof(char));
		strcpy(payload->metrics[size-1].value.string_value, (char *)value);
	} else if (datatype == METRIC_DATA_TYPE_BYTES) {
		DEBUG_PRINT(("Datatype BYTES - Not yet supported\n"));
	} else if (datatype == METRIC_DATA_TYPE_DATASET) {
		DEBUG_PRINT(("Datatype DATASET - Not yet supported\n"));
	} else if (datatype == METRIC_DATA_TYPE_FILE) {
		DEBUG_PRINT(("Datatype FILE - Not yet supported\n"));
	} else if (datatype == METRIC_DATA_TYPE_TEMPLATE) {
		DEBUG_PRINT(("Setting datatype: %zd, with value: %d\n", datatype, *((bool *)value)));
		payload->metrics[size-1].which_value = org_eclipse_tahu_protobuf_Payload_Metric_template_value_tag;
		payload->metrics[size-1].value.template_value = *((org_eclipse_tahu_protobuf_Payload_Template *)value);
	} else {
		DEBUG_PRINT(("Unknown datatype %ju\n", datatype));
	}

	payload->metrics_count++;
	DEBUG_PRINT(("Size of metrics payload %d\n", payload->metrics_count));
}

/*
 * Encode a Payload into an array of bytes
 */
size_t encode_payload(uint8_t **buffer, size_t buffer_length, org_eclipse_tahu_protobuf_Payload *payload) {
        size_t message_length;
	bool node_status;

	// Create the stream
	pb_ostream_t node_stream = pb_ostream_from_buffer(*buffer, buffer_length);

	// Encode the payload
	DEBUG_PRINT(("Encoding...\n"));
	node_status = pb_encode(&node_stream, org_eclipse_tahu_protobuf_Payload_fields, payload);
	message_length = node_stream.bytes_written;
	DEBUG_PRINT(("Message length: %zd\n", message_length));

        // Error Check
        if (!node_status) {
                fprintf(stderr, "Encoding failed: %s\n", PB_GET_ERROR(&node_stream));
                return -1;
        } else {
                DEBUG_PRINT(("Encoding succeeded\n"));
		return message_length;
        }
}

/*
 * Decode an array of bytes into a Payload
 */
bool decode_payload(org_eclipse_tahu_protobuf_Payload *payload, const void *binary_payload, int binary_payloadlen) {

	pb_istream_t stream = pb_istream_from_buffer(binary_payload, binary_payloadlen);
	DEBUG_PRINT(("Bytes Remaining: %zd\n", stream.bytes_left));

	// Local vars for payload decoding
	bool status;
	pb_wire_type_t payload_wire_type;
	uint32_t payload_tag;
	bool payload_eof;
	const pb_field_t *payload_field;

	// Loop over blocks while decoding portions of the payload
	while (pb_decode_tag(&stream, &payload_wire_type, &payload_tag, &payload_eof)) {
		DEBUG_PRINT(("payload_eof: %s\n", payload_eof ? "true" : "false"));
		DEBUG_PRINT(("\tBytes Remaining: %zd\n", stream.bytes_left));
		DEBUG_PRINT(("\tWiretype: %d\n", payload_wire_type));
		DEBUG_PRINT(("\tTag: %d\n", payload_tag));

		if (payload_wire_type == PB_WT_VARINT) {
			for (payload_field = org_eclipse_tahu_protobuf_Payload_fields; payload_field->tag != 0; payload_field++) {
				if (payload_field->tag == payload_tag && (((payload_field->type & PB_LTYPE_VARINT) == PB_LTYPE_VARINT) ||
										((payload_field->type & PB_LTYPE_UVARINT) == PB_LTYPE_UVARINT))) {
					DEBUG_PRINT(("\tWire type is PB_WT_VARINT\n"));
					uint64_t dest;
					status = pb_decode_varint(&stream, &dest);
					if (status) {
						DEBUG_PRINT(("\tVARINT - Success - new value: %ld\n", dest));
					} else {
						fprintf(stderr, "\tVARINT - Failed to decode variant!\n");
						return false;
					}

					if (payload_field->tag == org_eclipse_tahu_protobuf_Payload_timestamp_tag) {
						payload->has_timestamp = true;
						payload->timestamp = dest;
					} else if (payload_field->tag == org_eclipse_tahu_protobuf_Payload_seq_tag) {
						payload->has_seq = true;
						payload->seq = dest;
					}
				} else if (payload_field->tag == payload_tag && ((payload_field->type & PB_LTYPE_SVARINT) == PB_LTYPE_SVARINT)) {
					DEBUG_PRINT(("\tWire type is PB_WT_SVARINT\n"));
					int64_t dest;
					status = pb_decode_svarint(&stream, &dest);
					if (status) {
						DEBUG_PRINT(("\tVARINT - Success - new value: %ld\n", dest));
					} else {
						fprintf(stderr, "\tVARINT - Failed to decode variant!\n");
						return false;
					}
				}
			}
		} else if (payload_wire_type == PB_WT_64BIT) {
			DEBUG_PRINT(("\tWire type is PB_WT_64BIT\n"));
		} else if (payload_wire_type == PB_WT_STRING) {
			DEBUG_PRINT(("\tWire type is PB_WT_STRING\n"));
			for (payload_field = org_eclipse_tahu_protobuf_Payload_fields; payload_field->tag != 0; payload_field++) {
				if (payload_field->tag == payload_tag && ((payload_field->type & PB_LTYPE_SUBMESSAGE) == PB_LTYPE_SUBMESSAGE)) {
					DEBUG_PRINT(("\tFound a PB_LTYPE_SUBMESSAGE\n"));

					// This is a metric!
					if (payload_field->ptr == NULL) {
						fprintf(stderr, "Invalid field descriptor\n");
						return false;
					}

					org_eclipse_tahu_protobuf_Payload_Metric metric = org_eclipse_tahu_protobuf_Payload_Metric_init_zero;
					if(decode_metric(&metric, &stream)) {
						DEBUG_PRINT(("Decoding metric succeeded\n"));
						add_metric_to_payload(payload, &metric);
					} else {
						fprintf(stderr, "Decoding metric failed\n");
						return false;
					}
				} else if (payload_field->tag == payload_tag && ((payload_field->type & PB_LTYPE_FIXED_LENGTH_BYTES) == PB_LTYPE_FIXED_LENGTH_BYTES)) {
					DEBUG_PRINT(("\tFound a PB_LTYPE_FIXED_LENGTH_BYTES\n"));
				} else if (payload_field->tag == payload_tag && ((payload_field->type & PB_LTYPE_STRING) == PB_LTYPE_STRING)) {
					DEBUG_PRINT(("\tFound a PB_LTYPE_STRING\n"));

					// Get the UUID
					pb_byte_t string_size[1];
					status = pb_read(&stream, string_size, 1);
					if (status) {
						DEBUG_PRINT(("\t\tUUID Size: %d\n", string_size[0]));
					} else {
						fprintf(stderr, "\t\tFailed to read the UUID\n");
						return false;
					}

					pb_byte_t dest[string_size[0]+1];
					status = pb_read(&stream, dest, string_size[0]);
					if (status) {
						dest[string_size[0]] = '\0';
						DEBUG_PRINT(("\t\tRead the UUID: %s\n", dest));
						payload->uuid = (char *)malloc((strlen(dest)+1)*sizeof(char));;
						strcpy(payload->uuid, dest);
					} else {
						fprintf(stderr, "\t\tFailed to read the UUID...\n");
						return false;
					}


				} else if (payload_field->tag == payload_tag && ((payload_field->type & PB_LTYPE_BYTES) == PB_LTYPE_BYTES)) {
					DEBUG_PRINT(("\tFound a PB_LTYPE_BYTES\n"));
				//} else {
					//DEBUG_PRINT(("\tother: %d\n", payload_field->type);
				}
			}
		} else if (payload_wire_type == PB_WT_32BIT) {
			DEBUG_PRINT(("\tWire type is PB_WT_32BIT\n"));
		} else {
			fprintf(stderr, "\tUnknown wiretype...\n");
		}
	}

#ifdef SPARKPLUG_DEBUG
	// Print the message data
	print_payload(payload);
#endif

	return true;
}

/*
 * Free memory from an existing Payload
 */
void free_payload(org_eclipse_tahu_protobuf_Payload *payload) {
	int i=0;
	for (i=0; i<payload->metrics_count; i++) {
		free(payload->metrics[i].name);
		// More TODO...
		// JPL 04/05/17... free up string data allocated memory
		if(  payload->metrics[i].which_value == 
			 org_eclipse_tahu_protobuf_Payload_Metric_string_value_tag ) // 15 ???
		{
		  if(payload->metrics[i].value.string_value)  // not null?
		  {
			free(payload->metrics[i].value.string_value);
		  }
		}
		
		if (payload->metrics[i].has_properties)
		{
			int j=0;
			for (j=0; j<payload->metrics[i].properties.keys_count; j++){
				free(payload->metrics[i].properties.keys[j]);
				
				if(payload->metrics[i].properties.values[j].which_value ==
					com_cirruslink_sparkplug_protobuf_Payload_PropertyValue_string_value_tag) 
				{
					if(payload->metrics[i].properties.values[j].value.string_value)
					{	
						free(payload->metrics[i].properties.values[j].value.string_value);
					}
				}
			}
			free(payload->metrics[i].properties.keys);
			free(payload->metrics[i].properties.values);	
		}	
		
	}
}

/*
 * Get the current timestamp in milliseconds
 */
uint64_t get_current_timestamp() {
	// Set the timestamp
	struct timespec ts;
	#ifdef __MACH__ // OS X does not have clock_gettime, use clock_get_time
		clock_serv_t cclock;
		mach_timespec_t mts;
		host_get_clock_service(mach_host_self(), CALENDAR_CLOCK, &cclock);
		clock_get_time(cclock, &mts);
		mach_port_deallocate(mach_task_self(), cclock);
		ts.tv_sec = mts.tv_sec;
		ts.tv_nsec = mts.tv_nsec;
	#else
		clock_gettime(CLOCK_REALTIME, &ts);
	#endif
	return ts.tv_sec * UINT64_C(1000) + ts.tv_nsec / 1000000;
}

/*
 * Get the next empty Payload.  This populates the payload with the next sequence number and current timestamp
 */
void get_next_payload(org_eclipse_tahu_protobuf_Payload *payload) {
	// Initialize payload
	DEBUG_PRINT(("Current Sequence Number: %zd\n", seq));
	payload->has_timestamp = true;
	payload->timestamp = get_current_timestamp();
	payload->metrics_count = 0;
	payload->metrics = NULL;
	payload->has_seq = true;
	payload->seq = seq;
	payload->uuid = NULL;
	payload->body = NULL;
	payload->extensions = NULL;

	// Increment/wrap the sequence number
	if (seq == 255) {
		seq = 0;
	} else {
		seq++;
	}
}

/*
 * Initialize a Dataset with the values passed in
 */
void init_dataset(org_eclipse_tahu_protobuf_Payload_DataSet *dataset,
			uint64_t num_of_rows,
			uint64_t num_of_columns,
			uint32_t *datatypes,
			const char **column_keys,
			org_eclipse_tahu_protobuf_Payload_DataSet_Row *row_data) {

	// Set the number of columns
	dataset->has_num_of_columns = true;
	dataset->num_of_columns = num_of_columns;
	dataset->columns_count = num_of_columns;

	// Set up the column headers
	dataset->columns = (char **) calloc(num_of_columns, sizeof(char*));
	int i,j;
	for (i=0; i<num_of_columns; i++) {
		fprintf(stdout, "column_keys[i]: %s\n", column_keys[i]);
		dataset->columns[i] = (char *)malloc((strlen(column_keys[i])+1)*sizeof(char));
		strcpy(dataset->columns[i], column_keys[i]);
	}

	// Set the datatypes of the columns
	dataset->types_count = num_of_columns;
	dataset->types = datatypes;

	// Set the rows
	dataset->rows_count = num_of_rows;
	dataset->rows = row_data;
}

/*
 * Initialize a Metric with the values of the arguments passed in
 */
void init_metric(org_eclipse_tahu_protobuf_Payload_Metric *metric,
			const char *name,
			bool has_alias,
			uint64_t alias,
			uint64_t datatype,
			bool is_historical,
			bool is_transient,
			bool is_null,
			const void *value,
			size_t size_of_value) {

	if( name == NULL ) {
		DEBUG_PRINT(("Name is null"));
		metric->name = NULL;
	} else {
		metric->name = (char *)malloc((strlen(name)+1)*sizeof(char));
		strcpy(metric->name, name);
	}

	metric->has_alias = has_alias;
	if (has_alias) {
		metric->alias = alias;
	}
	if( is_historical && !is_transient )  // JPL 04/04/17... only timestamp historical
	{
	  metric->has_timestamp = true;
	  metric->timestamp = get_current_timestamp();
	}
	else
	{
	  metric->has_timestamp = false;
	  metric->timestamp = 0; //get_current_timestamp();
	}
	metric->has_datatype = true;
	metric->datatype = datatype;
	metric->has_is_historical = is_historical;
	if (is_historical) {
		metric->is_historical = is_historical;
	}
	metric->has_is_transient = is_transient;
	if (is_transient) {
		metric->is_transient = is_transient;
	}
	metric->has_is_null = is_null;
	if (is_null) {
		metric->is_null = is_null;
	}
	metric->has_metadata = false;
	metric->has_properties = false;

	// Default dynamically allocated members to NULL
	metric->value.string_value = NULL;

	DEBUG_PRINT(("Setting datatype and value - value size is %zd\n", size_of_value));
	if (datatype == METRIC_DATA_TYPE_UNKNOWN) {
		fprintf(stderr, "Can't create metric with unknown datatype!\n");
	} else if (datatype == METRIC_DATA_TYPE_INT8) {
		DEBUG_PRINT(("Setting datatype: %zd, with value: %d\n", datatype, *((int8_t *)value)));
		metric->which_value = org_eclipse_tahu_protobuf_Payload_Metric_int_value_tag;
		metric->value.int_value = *((int8_t *)value);
	} else if (datatype == METRIC_DATA_TYPE_INT16) {
		DEBUG_PRINT(("Setting datatype: %zd, with value: %d\n", datatype, *((int16_t *)value)));
		metric->which_value = org_eclipse_tahu_protobuf_Payload_Metric_int_value_tag;
		metric->value.int_value = *((int16_t *)value);
	} else if (datatype == METRIC_DATA_TYPE_INT32) {
		DEBUG_PRINT(("Setting datatype: %zd, with value: %d\n", datatype, *((int32_t *)value)));
		metric->which_value = org_eclipse_tahu_protobuf_Payload_Metric_int_value_tag;
		metric->value.int_value = *((int32_t *)value);
	} else if (datatype == METRIC_DATA_TYPE_INT64) {
		DEBUG_PRINT(("Setting datatype: %zd, with value: %zd\n", datatype, *((int64_t *)value)));
		metric->which_value = org_eclipse_tahu_protobuf_Payload_Metric_long_value_tag;
		metric->value.long_value = *((int64_t *)value);
	} else if (datatype == METRIC_DATA_TYPE_UINT8) {
		DEBUG_PRINT(("Setting datatype: %zd, with value: %u\n", datatype, *((uint8_t *)value)));
		metric->which_value = org_eclipse_tahu_protobuf_Payload_Metric_int_value_tag;
		metric->value.int_value = *((uint8_t *)value);
	} else if (datatype == METRIC_DATA_TYPE_UINT16) {
		DEBUG_PRINT(("Setting datatype: %zd, with value: %u\n", datatype, *((uint16_t *)value)));
		metric->which_value = org_eclipse_tahu_protobuf_Payload_Metric_int_value_tag;
		metric->value.int_value = *((uint16_t *)value);
	} else if (datatype == METRIC_DATA_TYPE_UINT32) {
		DEBUG_PRINT(("Setting datatype: %zd, with value: %u\n", datatype, *((uint32_t *)value)));
		metric->which_value = org_eclipse_tahu_protobuf_Payload_Metric_long_value_tag;
		metric->value.long_value = *((uint32_t *)value);
	} else if (datatype == METRIC_DATA_TYPE_UINT64) {
		DEBUG_PRINT(("Setting datatype: %zd, with value: %zd\n", datatype, *((uint64_t *)value)));
		metric->which_value = org_eclipse_tahu_protobuf_Payload_Metric_long_value_tag;
		metric->value.long_value = *((uint64_t *)value);
	} else if (datatype == METRIC_DATA_TYPE_DATETIME) {
		DEBUG_PRINT(("Setting datatype: %zd, with value: %zd\n", datatype, *((uint64_t *)value)));
		metric->which_value = org_eclipse_tahu_protobuf_Payload_Metric_long_value_tag;
		metric->value.long_value = *((uint64_t *)value);
	} else if (datatype == METRIC_DATA_TYPE_FLOAT) {
		DEBUG_PRINT(("Setting datatype: %zd, with value: %f\n", datatype, *((float *)value)));
		metric->which_value = org_eclipse_tahu_protobuf_Payload_Metric_float_value_tag;
		metric->value.float_value = *((float *)value);
	} else if (datatype == METRIC_DATA_TYPE_DOUBLE) {
		DEBUG_PRINT(("Setting datatype: %zd, with value: %f\n", datatype, *((double *)value)));
		metric->which_value = org_eclipse_tahu_protobuf_Payload_Metric_double_value_tag;
		metric->value.double_value = *((double *)value);
	} else if (datatype == METRIC_DATA_TYPE_BOOLEAN) {
		DEBUG_PRINT(("Setting datatype: %zd, with value: %d\n", datatype, *((bool *)value)));
		metric->which_value = org_eclipse_tahu_protobuf_Payload_Metric_boolean_value_tag;
		metric->value.boolean_value = *((bool *)value);
	} else if (datatype == METRIC_DATA_TYPE_STRING || datatype == METRIC_DATA_TYPE_TEXT || datatype == METRIC_DATA_TYPE_UUID) {
		DEBUG_PRINT(("Setting datatype: %zd, with value: %s\n", datatype, (char *)value));
		metric->which_value = org_eclipse_tahu_protobuf_Payload_Metric_string_value_tag;
		metric->value.string_value = (char *)malloc(size_of_value*sizeof(char));
		strcpy(metric->value.string_value, (char *)value);
	} else if (datatype == METRIC_DATA_TYPE_BYTES) {
		fprintf(stderr, "Datatype BYTES - Not yet supported\n");
	} else if (datatype == METRIC_DATA_TYPE_DATASET) {
		DEBUG_PRINT(("Setting datatype: %zd, with value: %d\n", datatype, *((bool *)value)));
		metric->which_value = org_eclipse_tahu_protobuf_Payload_Metric_dataset_value_tag;
		metric->value.dataset_value = *((org_eclipse_tahu_protobuf_Payload_DataSet *)value);
	} else if (datatype == METRIC_DATA_TYPE_FILE) {
		fprintf(stderr, "Datatype FILE - Not yet supported\n");
	} else if (datatype == METRIC_DATA_TYPE_TEMPLATE) {
		DEBUG_PRINT(("Setting datatype: %zd, with # of metrics: %d\n", datatype, ((org_eclipse_tahu_protobuf_Payload_Template *)value)->metrics_count));
		metric->which_value = org_eclipse_tahu_protobuf_Payload_Metric_template_value_tag;
		metric->value.template_value = *((org_eclipse_tahu_protobuf_Payload_Template *)value);
	} else {
		fprintf(stderr, "Unknown datatype %ju\n", datatype);
	}
}

/*
 * Display a full Sparkplug Payload
 */
void print_payload(org_eclipse_tahu_protobuf_Payload *payload) {
	fprintf(stdout, "Payload:  has_timestamp: %s\n", payload->has_timestamp ? "true" : "false");
	if (payload->has_timestamp) {
		fprintf(stdout, "Payload:  timestamp: %zd\n", payload->timestamp);
	}
	fprintf(stdout, "Payload:  has_seq: %s\n", payload->has_seq ? "true" : "false");
	if (payload->has_seq) {
		fprintf(stdout, "Payload:  seq: %zd\n", payload->seq);
	}
	fprintf(stdout, "Payload:  UUID: %s\n", payload->uuid);

	fprintf(stdout, "Payload:  Size of metric array: %d\n", payload->metrics_count);
	int i=0;
	for (i=0; i<payload->metrics_count; i++) {
		fprintf(stdout, "Payload:  Metric %d:  name: %s\n", i, payload->metrics[i].name);
		fprintf(stdout, "Payload:  Metric %d:  has_alias: %s\n", i, payload->metrics[i].has_alias ? "true" : "false");
		if (payload->metrics[i].has_alias) {
			fprintf(stdout, "Payload:  Metric %d:  alias: %zd\n", i, payload->metrics[i].alias);
		}
		fprintf(stdout, "Payload:  Metric %d:  has_timestamp: %s\n", i, payload->metrics[i].has_timestamp ? "true" : "false");
		if (payload->metrics[i].has_timestamp) {
			fprintf(stdout, "Payload:  Metric %d:  timestamp: %zd\n", i, payload->metrics[i].timestamp);
		}
		fprintf(stdout, "Payload:  Metric %d:  has_datatype: %s\n", i, payload->metrics[i].has_datatype ? "true" : "false");
		if (payload->metrics[i].has_datatype) {
			fprintf(stdout, "Payload:  Metric %d:  datatype: %d\n", i, payload->metrics[i].datatype);
		}
		fprintf(stdout, "Payload:  Metric %d:  has_is_historical: %s\n", i, payload->metrics[i].has_is_historical ? "true" : "false");
		if (payload->metrics[i].has_is_historical) {
			fprintf(stdout, "Payload:  Metric %d:  is_historical: %s\n", i, payload->metrics[i].is_historical ? "true" : "false");
		}
		fprintf(stdout, "Payload:  Metric %d:  has_is_transient: %s\n", i, payload->metrics[i].has_is_transient ? "true" : "false");
		if (payload->metrics[i].has_is_transient) {
			fprintf(stdout, "Payload:  Metric %d:  is_transient: %s\n", i, payload->metrics[i].is_transient ? "true" : "false");
		}
		fprintf(stdout, "Payload:  Metric %d:  has_is_null: %s\n", i, payload->metrics[i].has_is_null ? "true" : "false");
		if (payload->metrics[i].has_is_null) {
			fprintf(stdout, "Payload:  Metric %d:  is_null: %s\n", i, payload->metrics[i].is_null ? "true" : "false");
		}
		fprintf(stdout, "Payload:  Metric %d:  has_metadata: %s\n", i, payload->metrics[i].has_metadata ? "true" : "false");
		fprintf(stdout, "Payload:  Metric %d:  has_properties: %s\n", i, payload->metrics[i].has_properties ? "true" : "false");

		if (payload->metrics[i].datatype == METRIC_DATA_TYPE_UNKNOWN) {
			fprintf(stdout, "Payload:  Metric %d:  datatype: unknown datatype!\n", i);
		} else if (payload->metrics[i].datatype == METRIC_DATA_TYPE_INT8 ||
					payload->metrics[i].datatype == METRIC_DATA_TYPE_INT16 ||
					payload->metrics[i].datatype == METRIC_DATA_TYPE_INT32 ||
					payload->metrics[i].datatype == METRIC_DATA_TYPE_UINT8 ||
					payload->metrics[i].datatype == METRIC_DATA_TYPE_UINT16) {
			fprintf(stdout, "Payload:  Metric %d:  datatype: %d, with value: %d\n", i, payload->metrics[i].datatype, payload->metrics[i].value.int_value);
		} else if (payload->metrics[i].datatype == METRIC_DATA_TYPE_UINT32 ||
					payload->metrics[i].datatype == METRIC_DATA_TYPE_INT64 ||
					payload->metrics[i].datatype == METRIC_DATA_TYPE_UINT64 ||
					payload->metrics[i].datatype == METRIC_DATA_TYPE_DATETIME) {
			fprintf(stdout, "Payload:  Metric %d:  datatype: %d, with value: %zd\n", i, payload->metrics[i].datatype, payload->metrics[i].value.long_value);
		} else if (payload->metrics[i].datatype == METRIC_DATA_TYPE_FLOAT) {
			fprintf(stdout, "Payload:  Metric %d:  datatype: %d, with value: %f\n", i, payload->metrics[i].datatype, payload->metrics[i].value.float_value);
		} else if (payload->metrics[i].datatype == METRIC_DATA_TYPE_DOUBLE) {
			fprintf(stdout, "Payload:  Metric %d:  datatype: %d, with value: %f\n", i, payload->metrics[i].datatype, payload->metrics[i].value.double_value);
		} else if (payload->metrics[i].datatype == METRIC_DATA_TYPE_BOOLEAN) {
			fprintf(stdout, "Payload:  Metric %d:  datatype: %d, with value: %d\n", i, payload->metrics[i].datatype, payload->metrics[i].value.boolean_value);
		} else if (payload->metrics[i].datatype == METRIC_DATA_TYPE_STRING ||
					payload->metrics[i].datatype == METRIC_DATA_TYPE_TEXT ||
					payload->metrics[i].datatype == METRIC_DATA_TYPE_UUID) {
			fprintf(stdout, "Payload:  Metric %d:  datatype: %d, with value: %s\n", i, payload->metrics[i].datatype, payload->metrics[i].value.string_value);
		} else if (payload->metrics[i].datatype == METRIC_DATA_TYPE_BYTES) {
			fprintf(stdout, "Payload:  Metric %d:  datatype BYTES - Not yet supported\n", i);
		} else if (payload->metrics[i].datatype == METRIC_DATA_TYPE_DATASET) {
			fprintf(stdout, "Payload:  Metric %d:  datatype DATASET - Not yet supported\n", i);
		} else if (payload->metrics[i].datatype == METRIC_DATA_TYPE_FILE) {
			fprintf(stdout, "Payload:  Metric %d:  datatype FILE - Not yet supported\n", i);
		} else if (payload->metrics[i].datatype == METRIC_DATA_TYPE_TEMPLATE) {
			//fprintf(stdout, "Payload:  Metric %d:  datatype: %d, with value: %zd\n", i, payload->metrics[i].datatype, payload->metrics[i].value.long_value);
			fprintf(stdout, "Payload:  Metric %d:  datatype: %d, with # of metrics: %d\n", i, payload->metrics[i].datatype, payload->metrics[i].value.template_value.metrics_count);
		} else {
			fprintf(stdout, "Payload:  Metric %d:  datatype: %d\n", i, payload->metrics[i].datatype);
		}
	}
}
