# Terraform Multi-cloud Deployment

# Provider configuration for AKS
provider \"azurerm\" {
  features {}
}

# Provider configuration for GKE
provider \"google\" {
  project = var.project_id
  region  = var.region
}

# Main resources will be defined here
resource \"azurerm_kubernetes_cluster\" \"main\" {
  name                = \"-aks\"
  location            = var.location
  resource_group_name = azurerm_resource_group.main.name
  dns_prefix          = \"aks\"
 
  default_node_pool {
    name       = \"default\"
    node_count = 2
    vm_size    = \"Standard_D2_v2\"
  }

  identity {
    type = \"SystemAssigned\"
  }
}
