package eu.glasskube.operator.matomo.dependent.mariadb

import eu.glasskube.kubernetes.api.model.metadata
import eu.glasskube.operator.mariadb.DatabasebRef
import eu.glasskube.operator.mariadb.Grant
import eu.glasskube.operator.mariadb.GrantSpec
import eu.glasskube.operator.mariadb.grantMariaDB
import eu.glasskube.operator.matomo.Matomo
import eu.glasskube.operator.matomo.MatomoReconciler
import eu.glasskube.operator.matomo.databaseName
import eu.glasskube.operator.matomo.databaseUser
import eu.glasskube.operator.matomo.mariaDBHost
import eu.glasskube.operator.matomo.resourceLabels
import io.javaoperatorsdk.operator.api.reconciler.Context
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUDKubernetesDependentResource
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependent

@KubernetesDependent(labelSelector = MatomoReconciler.SELECTOR)
class MatomoGrantMariaDB : CRUDKubernetesDependentResource<Grant, Matomo>(Grant::class.java) {

    override fun desired(primary: Matomo, context: Context<Matomo>) = grantMariaDB {
        metadata {
            name = primary.mariaDBHost
            namespace = primary.metadata.namespace
            labels = primary.resourceLabels
        }
        spec = GrantSpec(mariaDbRef = DatabasebRef(primary.mariaDBHost), database = primary.databaseName, username = primary.databaseUser)
    }
}
