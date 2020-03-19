Contains Java bindings for OpenDiamond.

# Install on Ubuntu using Apt

Add the GPG key
```bash
wget -qO- http://diamond.cs.cmu.edu/packages/zf.key | sudo apt-key add -
```

Add the following line to file `/etc/apt/sources.list.d/opendiamond.list`:
```
deb http://diamond.cs.cmu.edu/packages CODENAME main
```
We currently support CODENAME = xenial | bionic (i.e., Ubuntu 16.04/18.04).

Finally,
```bash
apt-get update
apt-get install opendiamond-java
```


## Compile from Source

Clone this git repo and run:
```
./gradlew build
```
