# Puppet manifest
#
# Required modules:
# puppetlabs/mysql
# puppetlabs/java
# example42/tomcat
# maestrodev/maven
#

# Variables
$mysql_root_password = "root"
$mysql_bsis_user = "bsis"
$mysql_bsis_user_password = "bsis"
$mysql_bsis_database_name = "bsis"
$git_branch = "master"

# defaults for Exec
Exec {
	path => ["/bin", "/sbin", "/usr/bin", "/usr/sbin", "/usr/local/bin", "/usr/local/sbin"],
	user => "root",
}

# Make sure package index is updated
exec { "apt-get update":
    command => "apt-get update",
    user => "root",
}

# Install required packages
Package { ensure => "installed" }
package { "git": }

#Install MySQL server
class { 'mysql::server':
	root_password => $mysql_root_password
}

#Install Java
class { "java": }

#Install tomcat
class { "tomcat": }

# Install Maven
class { "maven::maven":
	version => "3.0.3", 
}

#Create directory to store codebase
file {
"/git":
	ensure => "directory",
	#owner => "vagrant";
}

#Clone git repository
exec { "clone-repo":
	cwd => "/git",
	command => "git clone http://github.com/jembi/bsis.git",
	timeout	=>	3600,
	#unless => "test -d /git/bsis/.git",
}

#Checkout relevant branch
exec { "checkout-branch":
	cwd => "/git/bsis",
	command => "git checkout $git_branch",
	timeout	=>	3600,
	require	=> Exec["clone-repo"],
}

# Remove database
exec { "clean-database":
	command => "echo drop database $mysql_bsis_database_name | mysql -uroot -p$mysql_root_password",
	returns => [0, 1],
	require	=> Class["mysql::server"],
}

# Create bsis demo database
exec { "create-database":
	command => "echo create database $mysql_bsis_database_name | mysql -uroot -p$mysql_root_password",
	returns => [0, 1],
	require	=> Exec["clean-database"],
}

# Build using maven
exec { "mvn-build":
	cwd => "/git/bsis",
	command => "mvn clean install",
	timeout	=>	3600,
	returns => [0, 1],
	require	=> Class["maven::maven"],
}

# Deploy to tomcat
exec { "deploy":
	command => "sudo cp /git/bsis/target/bsis.war /var/lib/tomcat6/webapps/",
	returns => [0, 1],
	require	=> Exec["mvn-build"],
}

#Restart tomcat
exec { "restart-tomcat":
	command => "sudo /etc/init.d/tomcat6 restart",
	returns => [0, 1],
	require	=> Exec["deploy"],
}
