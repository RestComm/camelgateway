# RestComm Camel Gateway
Enables web developers to build server side interactive IN networks for mobile phones using SS7 Camel Application Part (CAP) protocol.

## Introduction 

CAMEL stands for Customized Applications for Mobile networks Enhanced Logic. It is a set of standards designed to work on either a GSM core network or on a UMTS network. CAMEL allows an operator to define services over standard GSM services/UMTS services. The CAMEL architecture is based on the Intelligent network (IN) standards, and uses the CAP protocol.

Many services can be created using CAMEL, especially, services relating to roaming subscribers:

 * Virtual Private Network (VPN)
 * Call Redirect Services (CRS)
 * Conditional Call Forwarding (CCF)
 * SMS

RestComm Camel Gateway is built on [RestComm jSS7](https://github.com/RestComm/jss7) and RestComm JSLEE Server.

## Documentation

It is contained in the download binary

## Downloads

Download source code and build from [here](https://github.com/RestComm/camelgateway/releases) or Continuous Delivery binary from [CloudBees](https://mobicents.ci.cloudbees.com/job/RestComm-Camel-Gateway/)

## Want to Contribute ? 

[See our Contributors Guide](https://github.com/RestComm/Restcomm-Core/wiki/Contribute-to-RestComm)

## Issue Tracking and Roadmap

[Issue Tracker](https://github.com/RestComm/camelgateway/issues)

## Questions ?

Please ask your question on [StackOverflow](http://stackoverflow.com/questions/tagged/restcomm) or the Google [public forum](http://groups.google.com/group/restcomm)

## License

RestComm Camel Gateway is lead by [TeleStax](http://www.telestax.com/), Inc. and developed collaboratively by a community of individual and enterprise contributors.

RestComm Camel Gateway is licensed under dual license policy. The default license is the Free Open Source GNU Affero GPL v3.0. Alternatively a commercial license can be obtained from Telestax ([contact form](http://www.telestax.com/contactus/#InquiryForm))


[![FOSSA Status](https://app.fossa.io/api/projects/git%2Bhttps%3A%2F%2Fgithub.com%2FRestComm%2Fcamelgateway.svg?type=large)](https://app.fossa.io/projects/git%2Bhttps%3A%2F%2Fgithub.com%2FRestComm%2Fcamelgateway?ref=badge_large)

## Continuous Integration and Delivery

[![RestComm Camel Gateway Continuous Job](http://www.cloudbees.com/sites/default/files/Button-Built-on-CB-1.png)](https://mobicents.ci.cloudbees.com/job/RestComm-Camel-Gateway//)
[![FOSSA Status](https://app.fossa.io/api/projects/git%2Bhttps%3A%2F%2Fgithub.com%2FRestComm%2Fcamelgateway.svg?type=shield)](https://app.fossa.io/projects/git%2Bhttps%3A%2F%2Fgithub.com%2FRestComm%2Fcamelgateway?ref=badge_shield)

## Acknowledgements
[See who has been contributing to RestComm](http://www.telestax.com/opensource/acknowledgments/)

## Maven Repository

Artifacts are available at [Sonatype Maven Repo](https://oss.sonatype.org/content/repositories/releases/org/mobicents) which are also synched to central

## Wiki

Read our [RestComm Camel Gateway wiki](https://github.com/RestComm/camelgateway/wiki) 

# Testing 
To test Camel Gateway with RestComm ss7-simulator make sure you follow the below configuration changes and the execute SS7 Command's
Assume you are using Camel GW version 3.0.0

1) Download and extract restcomm-camel-3.0.0.zip form [Sonatype Camel GW Repo](https://mobicents.ci.cloudbees.com/job/RestComm-Camel-Gateway/4/artifact/release/)
 
2) Set JBOSS_HOME to restcomm-camel-3.0.0/jboss-5.1.0.GA

	2.1) export JBOSS_HOME=/path/to/restcomm-camel-3.0.0/jboss-5.1.0.GA

3) If you are deploying RestComm Camel Gateway from source code, goto release directory and execute ant release. It will create a bundle of every you need (and documents also) to run Restcomm Camel Gateway

4) Start Restcomm Camel Gateway in simulator mode

	4.1) ./run.sh -b 0.0.0.0 -c simulator

5) Run jss7 camel simulator

	5.1) jSS7 Camel Simulator is placed in <jboss>/tools/restcomm-jss7-simulator.

        cd <jboss>/tools/restcomm-jss7-simulator
        ./run.sh gui


	5.2) Choose Start/CAP_TEST_SCF/Run test

	5.3) CLick Start

	5.4) Check Camel Gateway functionality with Wireshark

6) Check Document Restcomm_CAMELGateway_ADmin_Guild.pdf for advance configuration