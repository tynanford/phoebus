/**
 * Copyright (C) 2018 European Spallation Source ERIC.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.phoebus.applications.saveandrestore.data.providers.jmasar;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import org.phoebus.applications.saveandrestore.data.DataProviderException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import se.esss.ics.masar.model.*;

import java.util.List;

public class JMasarJerseyClient implements JMasarClient{

    @Autowired
    private Client client;

    private static final String CONTENT_TYPE_JSON = "application/json; charset=UTF-8";

    @Value("${jmasar.service.url}")
    private String jmasarServiceUrl;

    public JMasarJerseyClient() {

        DefaultClientConfig defaultClientConfig = new DefaultClientConfig();
        defaultClientConfig.getProperties().put(ClientConfig.PROPERTY_READ_TIMEOUT, 1000);
        defaultClientConfig.getProperties().put(ClientConfig.PROPERTY_CONNECT_TIMEOUT, 1000);
        defaultClientConfig.getClasses().add(JacksonJsonProvider.class);
        client = Client.create(defaultClientConfig);
    }



    @Override
    public String getServiceUrl() {
        return jmasarServiceUrl;
    }

    @Override
    public Node getRoot(){
        return getCall("/root", Node.class);
    }

    @Override
    public Node getNode(String uniqueNodeId){
        return getCall("/node/" + uniqueNodeId, Node.class);
    }

    @Override
    public Node getParentNode(String unqiueNodeId){
        return getCall("/node/" + unqiueNodeId + "/parent", Node.class);
    }

    @Override
    public List<Node> getChildNodes(Node node) throws DataProviderException{
        ClientResponse response;
        if(node.getNodeType().equals(NodeType.CONFIGURATION)){
            response = getCall("/config/" + node.getUniqueId() + "/snapshots");
        }
        else{
            response = getCall("/node/" + node.getUniqueId() + "/children");
        }
        return response.getEntity(new GenericType<>(){});
    }

    @Override
    public List<SnapshotItem> getSnapshotItems(String snapshotUniqueId){
        ClientResponse response = getCall("/snapshot/" + snapshotUniqueId + "/items");
        return response.getEntity(new GenericType<>(){});
    }

    @Override
    public Node saveSnapshot(String configUniqueId, List<SnapshotItem> snapshotItems, String snapshotName, String comment){
        WebResource webResource = client.resource(jmasarServiceUrl + "/snapshot/" + configUniqueId)
                .queryParam("snapshotName", snapshotName)
                .queryParam("comment", comment)
                .queryParam("userName", getCurrentUsersName());
        ClientResponse response = webResource.accept(CONTENT_TYPE_JSON)
                .entity(snapshotItems, CONTENT_TYPE_JSON)
                .put(ClientResponse.class);

        if (response.getStatus() != 200) {
            throw new DataProviderException(response.getEntity(String.class));
        }

        return response.getEntity(Node.class);
    }

    @Override
    public List<ConfigPv> getConfigPvs(String configUniqueId){
        ClientResponse response = getCall("/config/" + configUniqueId + "/items");
        return response.getEntity(new GenericType<>(){});
    }

    @Override
    public Node createNewNode(String parentsUniqueId, Node node) {

        node.setUserName(getCurrentUsersName());

        WebResource webResource = client.resource(jmasarServiceUrl + "/node/" + parentsUniqueId);

        ClientResponse response = webResource.accept(CONTENT_TYPE_JSON)
                .entity(node, CONTENT_TYPE_JSON)
                .put(ClientResponse.class);
        if (response.getStatus() != 200) {
            throw new DataProviderException(response.getEntity(String.class));
        }

        return response.getEntity(Node.class);

    }

    @Override
    public Node updateNode(Node nodeToUpdate) {
        nodeToUpdate.setUserName(getCurrentUsersName());
        WebResource webResource = client.resource(jmasarServiceUrl + "/node/" + nodeToUpdate.getUniqueId() + "/update");

        ClientResponse response = webResource.accept(CONTENT_TYPE_JSON)
                .post(ClientResponse.class);

        if (response.getStatus() != 200) {
            throw new DataProviderException(response.getEntity(String.class));
        }

        return response.getEntity(Node.class);
    }

    @Override
    public Node takeSnapshot(String uniqueNodeId) {
        WebResource webResource = client.resource(jmasarServiceUrl + "/config/" + uniqueNodeId + "/snapshot");

        ClientResponse response = webResource.accept(CONTENT_TYPE_JSON).put(ClientResponse.class);
        if (response.getStatus() != 200) {
            throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
        }

        return response.getEntity(Node.class);
    }

    @Override
    public void tagSnapshotAsGolden(String uniqueNodeId){
        WebResource webResource = client.resource(jmasarServiceUrl + "/snapshot/" + uniqueNodeId + "/golden");

        ClientResponse response = webResource.accept(CONTENT_TYPE_JSON).post(ClientResponse.class);
        if (response.getStatus() != 200) {
            throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
        }
    }

    private <T> T getCall(String relativeUrl, Class<T> clazz) {

        ClientResponse response = getCall(relativeUrl);
        return response.getEntity(clazz);
    }

    private ClientResponse getCall(String relativeUrl){
        WebResource webResource = client.resource(jmasarServiceUrl + relativeUrl);

        ClientResponse response = webResource.accept(CONTENT_TYPE_JSON).get(ClientResponse.class);
        if (response.getStatus() != 200) {
            String message = response.getEntity(String.class);
            throw new DataProviderException("Failed : HTTP error code : " + response.getStatus() + ", error message: " + message);
        }

        return response;
    }

    @Override
    public void deleteNode(String uniqueNodeId) {
        WebResource webResource = client.resource(jmasarServiceUrl + "/node/" + uniqueNodeId);
        ClientResponse response = webResource.accept(CONTENT_TYPE_JSON).delete(ClientResponse.class);
        if (response.getStatus() != 200) {
            String message = response.getEntity(String.class);
            throw new DataProviderException("Failed : HTTP error code : " + response.getStatus() + ", error message: " + message);
        }
    }

    private String getCurrentUsersName(){
        return System.getProperty("user.name");
    }

    @Override
    public Node updateConfiguration(Node configToUpdate, List<ConfigPv> configPvList) {

        configToUpdate.setUserName(getCurrentUsersName());

        WebResource webResource = client.resource(jmasarServiceUrl + "/config/" + configToUpdate.getUniqueId() + "/update");

        UpdateConfigHolder holder = UpdateConfigHolder.builder()
                .config(configToUpdate)
                .configPvList(configPvList)
                .build();

        ClientResponse response = webResource.accept(CONTENT_TYPE_JSON)
                .entity(holder, CONTENT_TYPE_JSON)
                .post(ClientResponse.class);
        if (response.getStatus() != 200) {
            throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
        }

        return response.getEntity(Node.class);
    }

    @Override
    public String getJMasarServiceVersion(){
        WebResource webResource = client.resource(jmasarServiceUrl + "/version");

        ClientResponse response = webResource.get(ClientResponse.class);
        if (response.getStatus() != 200) {
            throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
        }

        return response.getEntity(String.class);
    }

    @Override
    public ConfigPv updateSingleConfigPv(String currentPvName, String newPvName, String currentReadbackPvName, String newReadbackPvName){
        WebResource webResource = client.resource(jmasarServiceUrl + "/configpv/" + currentPvName)
                .queryParam("newPvName", newPvName)
                .queryParam("readbackPvName", currentReadbackPvName)
                .queryParam("newReadbackPvName", newReadbackPvName);

        ClientResponse response = webResource.post(ClientResponse.class);
        if (response.getStatus() != 200) {
            throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
        }

        return response.getEntity(ConfigPv.class);
    }
}