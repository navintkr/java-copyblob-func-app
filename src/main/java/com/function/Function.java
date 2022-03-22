package com.function;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import com.azure.core.http.rest.PagedIterable;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobItem;


import java.util.Optional;

/**
 * Azure Functions with HTTP Trigger.
 */
public class Function {
    /**
     * This function listens at endpoint "/api/HttpExample". Two ways to invoke it using "curl" command in bash:
     * 1. curl -d "HTTP Body" {your host}/api/HttpExample
     * 2. curl "{your host}/api/HttpExample?name=HTTP%20Query"
     */
    @FunctionName("HttpExample")
    public HttpResponseMessage run(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET, HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS)
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        context.getLogger().info("Java HTTP trigger processed a request.");

        // Parse query parameter
        final String query = request.getQueryParameters().get("sourceContainerName");
        final String sourceContainerName = request.getBody().orElse(query);
        final String query2 = request.getQueryParameters().get("destinationContainerName");
        final String destinationContainerName = request.getBody().orElse(query2);

        
        if (sourceContainerName == null || destinationContainerName == null) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Please pass sourceContainerName and destinationContainerName on the query string or in the request body").build();
        } else {
            String connectStr = "<your Source SAS Token>";

            String destconnectStr="<your destination container's SAS token>";
    
    
    
            // Create a BlobServiceClient object which will be used to create a container client
            // BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().connectionString(connectStr).buildClient();
            BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().endpoint("https://{source storage account name}.blob.core.windows.net/").sasToken(connectStr).buildClient();
            BlobServiceClient destblobServiceClient = new BlobServiceClientBuilder().endpoint("https://{destination storage account name}.blob.core.windows.net/").sasToken(destconnectStr).buildClient();
    
            // BlobServiceClient destblobServiceClient = new BlobServiceClientBuilder().connectionString(destconnectStr).buildClient();
    
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(sourceContainerName);
    
            BlobContainerClient destcontainer=destblobServiceClient.getBlobContainerClient(destinationContainerName);
    
            PagedIterable<BlobItem> blobs= containerClient.listBlobs();
            for (BlobItem blobItem : blobs) {
    
                System.out.println("This is the blob name: " + blobItem.getName());
                BlobClient blobClient=containerClient.getBlobClient(blobItem.getName());
                BlobClient destblobclient=destcontainer.getBlobClient(blobItem.getName());
                destblobclient.beginCopy(blobClient.getBlobUrl(),null);
    
            }
            return request.createResponseBuilder(HttpStatus.OK).body("Moved blobs from " + sourceContainerName+" to "+destinationContainerName).build();
        }
    }
}
