# EMI
EMI is a featureful and accessible item and recipe viewer for Minecraft.

![EMI Interface](https://user-images.githubusercontent.com/14813658/224562247-1588064e-39ef-475a-9108-d7a357af6939.png)

![Recipe Tree](https://user-images.githubusercontent.com/14813658/224562258-1a5ee67a-fd7f-489f-9eed-ae67c184ddac.png)

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
	modCompileOnly "dev.emi:emi:${emi_version}:api"
	modLocalRuntime "dev.emi:emi:${emi_version}"
}
```
