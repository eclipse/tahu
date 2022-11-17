# Eclipse Tahu

Eclipse Tahu provide client libraries and reference implementations in various languages and for various devices
to show how the device/remote application must connect and disconnect from the MQTT server using the Sparkplug
specification explained below.  This includes device lifecycle messages such as the required birth and last will &
testament messages that must be sent to ensure the device lifecycle state and data integrity.

# Sparkplug

Sparkplug®, Sparkplug Compatible, and the Sparkplug Logo are trademarks of the Eclipse Foundation.

Sparkplug is a specification for MQTT enabled devices and applications to send and receive messages in a stateful way.
While MQTT is stateful by nature it doesn't ensure that all data on a receiving MQTT application is current or valid.
Sparkplug provides a mechanism for ensuring that remote device or application data is current and valid.

Sparkplug A was the original version of the Sparkplug specification and used Eclipse Kura's protobuf definition for
payload encoding.  However, it was quickly determined that this definition was too limited to handle the metadata that
typical Sparkplug payloads require.  As a result, Sparkplug B was developed to add additional features and capabilities
that were not possible in the original Kura payload definition.  These features include:
* Complex data types using templates
* Datasets
* Richer metrics with the ability to add property metadata for each metric
* Metric alias support to maintain rich metric naming while keeping bandwidth usage to a minimum
* Historical data
* File data

Sparkplug Specification v3.0.0: https://www.eclipse.org/tahu/spec/sparkplug_spec.pdf
Eclipse Sparkplug Project: https://projects.eclipse.org/projects/iot.sparkplug
Eclipse Sparkplug & TCK Github Repository: https://github.com/eclipse-sparkplug/sparkplug

# Contributing
Contributing to the Sparkplug Tahu Project is easy and contributions are welcome. In order to submit a pull request (PR) you must follow these steps. Failure to follow these steps will likely lead to the PR being rejected.
1. Sign the Eclipse Contributor Agreement (ECA): https://accounts.eclipse.org/user/eca
2. Make sure the email tied to your Github account is the same one you used to sign the ECA.
3. Submit your PR against the develop branch of the repository. PRs against master will not be accepted: https://github.com/eclipse/sparkplug/tree/develop
4. Sign off on your PR using the '-s' flag. For example: 'git commit -m"My brief comment" ChangedFile'
5. Make sure to include any important context or information associated with the PR in the PR submission. Keep your commit comment brief.
