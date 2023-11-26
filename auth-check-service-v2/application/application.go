package application

import (
	"log"
	"os"

	"gopkg.in/yaml.v3"
)

type Application struct {
	Config *ApplicationConfig
}

type ApplicationDatabaseConfig struct {
	DatabaseUrl      string `yaml:"database-url"`
	DatabaseUser     string `yaml:"database-user"`
	DatabasePassword string `yaml:"database-password"`
	MaxPoolSize      int32  `yaml:"max-pool-size"`
}

type ApplicationConfig struct {
	Database ApplicationDatabaseConfig `yaml:"database"`
}

func NewApplication() *Application {
	config := getConfig()
	app := Application{
		Config: config,
	}
	return &app
}

// Возвращает конфигурацию, заданную в yml-файле приложения.
func getConfig() *ApplicationConfig {
	configFile, err := os.Open("config.yml")
	if err != nil {
		log.Fatal(err)
	}
	defer configFile.Close()

	var config ApplicationConfig
	decoder := yaml.NewDecoder(configFile)
	err = decoder.Decode(&config)
	if err != nil {
		log.Fatal(err)
	}

	return &config
}
