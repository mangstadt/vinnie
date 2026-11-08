# vinnie

|     |     |
| --- | --- |
| Maven Central: | https://central.sonatype.com/artifact/com.github.mangstadt/vinnie |
| License: | [![MIT License](https://img.shields.io/badge/License-MIT-red.svg)](https://github.com/mangstadt/vinnie/blob/master/LICENSES) |

vinnie is a lightweight Java library that reads and writes "vobject" data (vCard and iCalendar).  It is used by the [ez-vcard](https://github.com/mangstadt/ez-vcard) and [biweekly](https://github.com/mangstadt/biweekly) projects.

<p align="center"><strong><a href="https://github.com/mangstadt/vinnie/wiki/Downloads">Downloads</a> |
<a href="http://mangstadt.github.io/vinnie/javadocs/latest/index.html">Javadocs</a> |
<a href="#mavengradle">Maven/Gradle</a> | <a href="https://github.com/mangstadt/vinnie/wiki">Documentation</a></strong></p>

# Examples

## Parsing

**Code**

```java
String str =
"BEGIN:VCARD\r\n" +
"VERSION:2.1\r\n" +
"FN:John Doe\r\n" +
"NOTE;QUOTED-PRINTABLE;CHARSET=UTF-8:=C2=A1Hola, mundo!\r\n" +
"END:VCARD\r\n";

Reader reader = new StringReader(str);
SyntaxRules rules = SyntaxRules.vcard();
try (VObjectReader vobjectReader = new VObjectReader(reader, rules)){
	vobjectReader.parse(new VObjectDataAdapter() {
		private boolean inVCard = false;

		public void onComponentBegin(LaciToken name, Context context) {
			if (context.getParentComponents().isEmpty() && name.equalsIgnoreCase("VCARD")){
				inVCard = true;
			}
		}

		public void onComponentEnd(LaciToken name, Context context) {
			if (context.getParentComponents().isEmpty()) {
				//end of vCard, stop parsing
				context.stop();
			}
		}

		public void onProperty(VObjectProperty property, Context context) {
			if (inVCard) {
				System.out.println(property.getName() + " = " + property.getValue());
			}
		}
	});
}
```

**Output**

```
FN = John Doe
NOTE = ¡Hola, mundo!
```

## Writing

**Code**

```java
Writer writer = new OutputStreamWriter(System.out);
try (VObjectWriter vobjectWriter = new VObjectWriter(writer, SyntaxStyle.LEGACY)) {
	vobjectWriter.writeBeginComponent("VCARD");
	vobjectWriter.writeVersion("2.1");
	vobjectWriter.writeProperty("FN", "John Doe");

	VObjectProperty note = new VObjectProperty("NOTE", "¡Hola, mundo!");
	note.getParameters().put((String) null, "QUOTED-PRINTABLE");
	vobjectWriter.writeProperty(note);

	vobjectWriter.writeEndComponent("VCARD");
}
```

**Output**

```
BEGIN:VCARD
VERSION:2.1
FN:John Doe
NOTE;QUOTED-PRINTABLE;CHARSET=UTF-8:=C2=A1Hola, mundo!
END:VCARD
```

# Features

 * Full ABNF compliance with vCard (versions 2.1, 3.0, and 4.0) and iCalendar (versions 1.0 and 2.0) specifications.
 * Automatic decoding/encoding of quoted-printable data.
 * Streaming API.
 * Extensive unit test coverage.
 * Low Java version requirement (1.8 or above).
 * No dependencies on external libraries.

# Maven/Gradle


**Maven**

```xml
<dependency>
   <groupId>com.github.mangstadt</groupId>
   <artifactId>vinnie</artifactId>
   <version>3.0.0</version>
</dependency>
```

**Gradle**

```
compile 'com.github.mangstadt:vinnie:3.0.0'
```

# Build Instructions

vinnie uses [Maven](http://maven.apache.org/) as its build tool, and adheres to its conventions.

To build the project: `mvn compile`  
To run the unit tests: `mvn test`  
To build a JAR: `mvn package`

# Questions / Feedback

 * Post an [issue](https://github.com/mangstadt/vinnie/issues)
 * Start a [discussion](https://github.com/mangstadt/vinnie/discussions)

[![](https://www.paypalobjects.com/en_US/i/btn/btn_donateCC_LG.gif)](https://www.paypal.com/cgi-bin/webscr?cmd=_donations&business=8CEN7MPKRBKU6&lc=US&item_name=Michael%20Angstadt&item_number=vinnie&currency_code=USD&bn=PP%2dDonationsBF%3abtn_donateCC_LG%2egif%3aNonHosted)
