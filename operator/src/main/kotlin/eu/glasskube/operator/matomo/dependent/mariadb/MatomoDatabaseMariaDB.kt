package eu.glasskube.operator.matomo.dependent.mariadb

import eu.glasskube.kubernetes.api.model.metadata
import eu.glasskube.operator.mariadb.Database
import eu.glasskube.operator.mariadb.DatabaseSpec
import eu.glasskube.operator.mariadb.DatabasebRef
import eu.glasskube.operator.mariadb.databaseMariaDB
import eu.glasskube.operator.matomo.Matomo
import eu.glasskube.operator.matomo.MatomoReconciler
import eu.glasskube.operator.matomo.databaseName
import eu.glasskube.operator.matomo.mariaDBHost
import eu.glasskube.operator.matomo.resourceLabels
import io.javaoperatorsdk.operator.api.reconciler.Context
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUDKubernetesDependentResource
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependent

@KubernetesDependent(labelSelector = MatomoReconciler.SELECTOR)
class MatomoDatabaseMariaDB : CRUDKubernetesDependentResource<Database, Matomo>(Database::class.java) {

    override fun desired(primary: Matomo, context: Context<Matomo>) = databaseMariaDB {
        metadata {
            name = primary.databaseName
            namespace = primary.metadata.namespace
            labels = primary.resourceLabels
        }
        spec = DatabaseSpec(
            mariaDbRef = DatabasebRef(primary.mariaDBHost)
        )
    }
}
