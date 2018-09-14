# s3-batch-uploader
Batch file uploader from a common file system to Amazon S3 (written in Java SE 1.6)

The entire code is licensed with Apache License, Version 2.0

This software was designed to run on UNIX-LIKE file systems.

In order to run this software, you must to write the properties file: 

```
aws_access_key_id = XYZ
aws_secret_access_key = XYZ 
bucketname=my-bucket-name
folder=C:\\
orderby=datedesc
overwrite=no
md5name=yes
fileaggregator=shp;dbf;shx
verbose=yes
log=yes
fileextension=shp;dbf;shx;jpg;tif
```

The fileextension property does not accept wildcards, the only exception is * which means all files.

The best way to run this software is:

```
$ java -jar s3-batch-upload.jar 
```

With this syntax the application will look for a properties file with this path **/etc/s3-batch-uploader/s3-batch-uploader.conf**

If you prefer to override the default properties file path, run the application with this syntax:

```
$ java -jar s3-batch-upload.jar properties-complete-file-path
```

At properties file, if the ```log``` property has the **yes** value, it will write an application log file in **/tmp/s3-batch-uploader.log**
