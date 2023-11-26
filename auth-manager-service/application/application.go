package application

import (
	"log"
	"net/http"
	"os"
	"time"

	"gopkg.in/yaml.v3"
)

type Application struct {
	Config     *ApplicationConfig
	HttpClient *http.Client
}

type ApplicationDatabaseConfig struct {
	DatabaseUrl      string `yaml:"database-url"`
	DatabaseUser     string `yaml:"database-user"`
	DatabasePassword string `yaml:"database-password"`
	MaxPoolSize      int32  `yaml:"max-pool-size"`
}

type ApplicationSigningConfig struct {
	PrivateKey string `yaml:"private-key"`
	PublicKey  string `yaml:"public-key"`
}

type ApplicationAuthCheckServiceConfig struct {
	Host string            `yaml:"host"`
	Urls map[string]string `yaml:"urls"`
}

type ApplicationConfig struct {
	Database         ApplicationDatabaseConfig         `yaml:"database"`
	Signing          ApplicationSigningConfig          `yaml:"signing"`
	AuthCheckService ApplicationAuthCheckServiceConfig `yaml:"auth-check-service"`
}

func NewApplication() *Application {
	config := getConfig()
	app := Application{
		Config:     config,
		HttpClient: &http.Client{Timeout: 30 * time.Second},
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
