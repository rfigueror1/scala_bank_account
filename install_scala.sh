scalaVer="2.12.8"
apt-get remove scala-library scala
wget www.scala-lang.org/files/archive/scala-"$scalaVer".deb
dpkg -i scala-"$scalaVer".deb
apt-get update
apt-get install scala
