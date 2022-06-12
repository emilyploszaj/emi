# EMI
EMI is a featureful and accessible item and recipe viewer for Minecraft.

![EMI Interface](https://i.imgur.com/Ctqbzkf.png)

## Developers
To add EMI to your project as a dependency you need to add the following to your `build.gradle`:
```gradle
repositories {
	maven {
		name = "TerraformersMC"
		url = "https://maven.terraformersmc.com/"
	}
}

dependencies {
	modImplementation "dev.emi:emi:${emi_version}"
}
```
