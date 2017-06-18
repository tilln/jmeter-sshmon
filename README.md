# jmeter-sshmon [![travis][travis-image]][travis-url]

[travis-image]: https://travis-ci.org/tilln/jmeter-sshmon.svg?branch=master
[travis-url]: https://travis-ci.org/tilln/jmeter-sshmon

Overview
--------

Apache JMeter Monitoring plugin for agentless collection and plotting of remote server metrics via SSH connections.

The output of remotely executed commands can be plotted over time and/or written to file (CSV/JTL).

![SSHMon Samples Collector](https://raw.githubusercontent.com/tilln/jmeter-sshmon/master/docs/sshmon_samples_collector.png)

This plugin is powered by [JMeter-Plugins](https://jmeter-plugins.org/) components.

Installation
------------
<!--
### Via [PluginsManager](https://jmeter-plugins.org/wiki/PluginsManager/)

Under tab "Available Plugins", select "SSHMon Sample Collector", then click "Apply Changes and Restart JMeter".

### Via Package from [JMeter-Plugins.org](https://jmeter-plugins.org/)

Extract the [zip package](https://jmeter-plugins.org/files/packages/tilln-sshmon-1.0.zip) into JMeter's lib directory, then restart JMeter.
-->

### Via Manual Download

1. Copy the [jmeter-sshmon jar file](https://github.com/tilln/jmeter-sshmon/releases/download/1.0-SNAPSHOT/jmeter-sshmon-1.0-SNAPSHOT.jar) into JMeter's lib/ext directory.
2. Copy the following dependencies into JMeter's lib directory:
	* [kg.apc / jmeter-plugins-cmn-jmeter](https://search.maven.org/remotecontent?filepath=kg/apc/jmeter-plugins-cmn-jmeter/0.5/jmeter-plugins-cmn-jmeter-0.5.jar)
    * [commons-io / commons-io](https://search.maven.org/remotecontent?filepath=commons-io/commons-io/2.5/commons-io-2.5.jar)
    * [commons-pool / ](https://search.maven.org/remotecontent?filepath=org/apache/commons/commons-pool2/2.4.2/commons-pool2-2.4.2.jar)
    * [com.jcraft.jsch / jsch](https://search.maven.org/remotecontent?filepath=com/jcraft/jsch/0.1.54/jsch-0.1.54.jar)
3. Restart JMeter.

**Important: Make sure to remove any older jar file version than `jmeter-plugins-cmn-jmeter-0.5.jar` from JMeter's lib directory!**

Usage
-----

From the context menu, select "Add" / "Listeners" / "SSHMon Samples Collector".

Add one row for each metric to be collected from a remote command, such as `sar`, `iostat`, `vmstat`, `mpstat` etc.
**Important:** The command *must* return a single decimal floating point number or it will fail!
That means commands that return multiple rows and/or columns of console output will have to be filtered accordingly (e.g. via grep, awk, sed etc).

### Example

|Label|Host|Port|Username|Private Key (PEM)|Password|Command|Delta|
|-----|----|----|--------|-----------------|--------|-------|-----|
|CPU%|127.0.0.1|22|jmeter||secret|<code>sar -u 1 1&#124;awk '/^Average:/{print 100-$8}'</code>|☐|

* Connect to localhost on TCP port 22 with username *jmeter* and password *secret*
* Get CPU utilisation from [`sar`](http://linuxcommand.org/man_pages/sar1.html) (1 sample, 1 second)
* Filter that output for the row starting with *Average*
* Take the 8<sup>th</sup> column (this may vary depending on the OS) as idle percentage and calculate the difference to 100%
* Record and/or plot the value with label *CPU%*

*Note that the password is not masked so it may be useful to define a variable or property (e.g. on the command line -Juser.password=secret).*

### Public Key Authentication

1. Enter the username in the username field.
2. Make the private key available in PEM format and supply the *content* in the private key field. In most cases when this comes from a file use the JMeter function [`${__FileToString(pem_file)}`](http://jmeter.apache.org/usermanual/functions.html#__FileToString).
3. Enter the password for the private key in the password field.
4. Make sure the public key is on the server.

### Cumulative Values

In some cases the difference between sample values may be more interesting than the absolute value.
The *Delta* check box can be ticked to record/plot the difference from the previous sample.

### Chart Generation

Charts can be generated in the following ways:
- In GUI mode, a live chart will be plotted that can be saved to an image file or copied to clipboard via right-click on the chart area.
- In GUI mode, an output file from a previous test can be loaded via the Filename text field.
- Without the GUI, the ["Command-Line Graph Plotting Tool"](https://jmeter-plugins.org/wiki/JMeterPluginsCMD/) may be used to generate charts by specifying the `--plugin-type` option like so:

  ```
  JMeterPluginsCMD.bat --plugin-type nz.co.breakpoint.jmeter.vizualizers.sshmon.SSHMonGui --input-jtl sshmon-results-filename.csv --generate-png output.png --width 800 --height 600
  ```
  For a full list of options refer to the [plugin documentation](https://jmeter-plugins.org/wiki/JMeterPluginsCMD/#Usage-and-Parameters).

Configuration
-------------

### Chart Settings

The tabs *Charts*, *Rows*, and *Settings* are only relevant in GUI mode.
For details refer to the [JMeter-Plugins wiki](https://jmeter-plugins.org/wiki/SettingsPanel/).

### JMeter Properties

The following properties control the plugin behaviour:
  * `jmeter.sshmon.knownHosts`: Filename of a known_hosts file containing public keys of trusted remote servers (in OpenSSH format). If not set, no validation will be performed.
  * `jmeter.sshmon.interval`: Define the metrics collection interval in milliseconds (default=1 second).
  * `jmeter.sshmon.forceOutputFile` - (true/false) makes sure JMeter writes metrics to CSV file in the current directory if no filename is specified (default=false).

Limitations
-----------

* Samples are collected by a single thread, so if the command takes more than an insignificant amount of time to run, the frequency of sample collection will be limited.
A separate monitor may be used in this case.
* The help link on the plugin GUI currently points to an incorrect location. This is to be resolved with the next release of `jmeter-plugins-cmn-jmeter` (see [this PR](https://github.com/undera/jmeter-plugins/pull/162)).
