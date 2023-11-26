package client

import (
	"bytes"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"os"

	app "openblocks.ru/api-gateway/auth-manager-service/application"
)

type CheckAuthRequest struct {
	Login    string `json:"login"`
	Password string `json:"password"`
}

type CheckAuthResponse struct {
	Status bool `json:"status"`
}

// Requests a service of authentication check to check login and password.
// Returns true in case if authentication check service approves correctness of password.
func CheckAuth(application *app.Application, login string, password string) (bool, error) {

	url := application.Config.AuthCheckService.Host +
		application.Config.AuthCheckService.Urls["authentication-check"]

	jsonRequest, err := json.Marshal(CheckAuthRequest{
		Login:    login,
		Password: password,
	})
	if err != nil {
		fmt.Fprintf(os.Stderr, "Cannot marshal request to auth-check-service: %v\n", err)
		return false, err
	}

	httpRequest, err := http.NewRequest("POST", url, bytes.NewReader(jsonRequest))
	httpRequest.Header.Set("Content-Type", "application/json; charset=UTF-8")

	httpClient := application.HttpClient

	httpResponse, err := httpClient.Do(httpRequest)
	if err != nil {
		fmt.Fprintf(os.Stderr, "Cannot perform request to auth-check-service: %v\n", err)
		return false, err
	}
	defer httpResponse.Body.Close()

	responseBody, err := io.ReadAll(httpResponse.Body)
	if err != nil {
		fmt.Fprintf(os.Stderr, "Cannot read response body of check auth: %v\n", err)
		return false, err
	}

	var jsonResponse CheckAuthResponse
	err = json.Unmarshal(responseBody, &jsonResponse)
	if err != nil {
		fmt.Fprintf(os.Stderr, "Cannot unmarshal response from auth-check-service from JSON: %v\n", err)
		return false, err
	}

	return jsonResponse.Status, nil
}
