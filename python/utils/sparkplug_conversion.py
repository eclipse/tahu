import json
import sparkplug_b as sparkplug
import sparkplug_b_pb2 as sparkplug_pb2


def encode_json_as_sparkplug_payload(json_data, use_datasets=False):
    """Convert JSON to Sparkplug B payload.

    Args:
        json_data (str): JSON to convert.
        use_datasets (bool, optional): Whether to use datasets in the
            payload. If False (default), the JSON data will be
            represented as metrics. If True, the JSON data will be
            represented as a dataset.

    Returns:
        bytes: Serialized Sparkplug B payload.
    """
    payload = sparkplug.Payload()

    # Parse the JSON data
    data = json.loads(json_data)

    if use_datasets:
        # Add JSON as a dataset to the payload
        dataset = sparkplug.initDatasetMetric(
            payload, name=None, alias=None,
            columns=list(data[0].keys()), types=[MetricDataType.String] * len(data[0])
        )
        for row in data:
            dataset.addRow(list(row.values()))
    else:
        # Add JSON data as metrics to the payload
        for key, value in data.items():
            sparkplug.addMetric(payload, key, value, sparkplug.MetricDataType.String)

    # Serialize the payload
    serialized_payload = payload.SerializeToString()
    return serialized_payload