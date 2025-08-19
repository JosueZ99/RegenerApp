"""
Management command para cargar datos iniciales en RegenerApp
Path: backend/apps/projects/management/commands/setup_initial_data.py
"""

from django.core.management.base import BaseCommand
from django.core.management import call_command
from django.db import transaction
from django.utils import timezone
from decimal import Decimal
from datetime import date, timedelta

class Command(BaseCommand):
    help = 'Configurar datos iniciales para RegenerApp - Versión 2.0'

    def add_arguments(self, parser):
        parser.add_argument(
            '--reset',
            action='store_true',
            help='Resetear base de datos antes de cargar',
        )

    def handle(self, *args, **options):
        """Ejecutar setup completo"""
        
        if options['reset']:
            self.stdout.write(
                self.style.WARNING('⚠️  Reseteando base de datos...')
            )
            self._reset_database()
        
        self.stdout.write(
            self.style.SUCCESS('🚀 Configurando RegenerApp con datos iniciales')
        )
        
        try:
            with transaction.atomic():
                self._create_categories()
                self._create_calculation_types()
                self._create_materials()
                self._create_suppliers()
                self._create_projects()
                self._create_supplier_prices()
                self._create_financial_summaries()
                
            self.stdout.write(
                self.style.SUCCESS('✅ Configuración completada exitosamente')
            )
            self._print_summary()
            
        except Exception as e:
            self.stdout.write(
                self.style.ERROR(f'❌ Error en la configuración: {str(e)}')
            )
            raise

    def _reset_database(self):
        """Limpiar datos existentes"""
        from django.apps import apps
        
        apps_to_clean = ['calculations', 'budgets', 'suppliers', 'materials', 'projects']
        
        for app_name in apps_to_clean:
            try:
                app_config = apps.get_app_config(app_name)
                for model in app_config.get_models():
                    count = model.objects.count()
                    if count > 0:
                        model.objects.all().delete()
                        self.stdout.write(f'  🗑️  Limpiado {count} registros de {model._meta.label}')
            except Exception as e:
                self.stdout.write(f'  ⚠️  Error limpiando {app_name}: {e}')

    def _create_categories(self):
        """Crear categorías de materiales"""
        from apps.materials.models import MaterialCategory
        
        self.stdout.write('📂 Creando categorías de materiales...')
        
        categories = [
            {
                'name': 'Pintura y Acabados',
                'category_type': 'construction',
                'description': 'Pinturas, selladores, imprimantes y acabados para construcción'
            },
            {
                'name': 'Drywall y Sistemas',
                'category_type': 'construction',
                'description': 'Paneles de gypsum, perfiles metálicos y accesorios'
            },
            {
                'name': 'Cintas y Tiras LED',
                'category_type': 'lighting',
                'description': 'Cintas LED, tiras flexibles y accesorios de iluminación LED'
            },
            {
                'name': 'Perfiles de Aluminio',
                'category_type': 'lighting',
                'description': 'Perfiles de aluminio para empotrar cintas LED'
            },
            {
                'name': 'Cables Eléctricos',
                'category_type': 'electrical',
                'description': 'Cables eléctricos, conductores y accesorios'
            },
            {
                'name': 'Controladores y Drivers',
                'category_type': 'lighting',
                'description': 'Drivers LED, controladores y fuentes de poder'
            }
        ]
        
        for cat_data in categories:
            category, created = MaterialCategory.objects.get_or_create(
                name=cat_data['name'],
                defaults=cat_data
            )
            if created:
                self.stdout.write(f'  ✓ Creada: {category.name}')

    def _create_calculation_types(self):
        """Crear tipos de calculadoras"""
        from apps.calculations.models import CalculationType
        
        self.stdout.write('🧮 Creando tipos de calculadoras...')
        
        calc_types = [
            {
                'name': 'Calculadora de Pintura',
                'code': 'paint',
                'category': 'construction',
                'description': 'Calcula la cantidad de pintura necesaria según área y número de capas',
                'input_parameters': {
                    'area_to_paint': {'type': 'decimal', 'label': 'Área a pintar (m²)', 'required': True},
                    'number_of_coats': {'type': 'integer', 'label': 'Número de capas', 'default': 2},
                    'paint_type': {'type': 'choice', 'label': 'Tipo de pintura', 'choices': ['Látex', 'Esmalte']},
                    'coverage_per_liter': {'type': 'decimal', 'label': 'Rendimiento (m²/L)', 'default': 10}
                }
            },
            {
                'name': 'Calculadora de Gypsum',
                'code': 'gypsum',
                'category': 'construction',
                'description': 'Calcula planchas de gypsum y perfiles necesarios',
                'input_parameters': {
                    'area_to_cover': {'type': 'decimal', 'label': 'Área a cubrir (m²)', 'required': True},
                    'thickness': {'type': 'decimal', 'label': 'Espesor (mm)', 'default': 12.7},
                    'gypsum_type': {'type': 'choice', 'label': 'Tipo', 'choices': ['Standard', 'Húmedo']}
                }
            },
            {
                'name': 'Calculadora de Cintas LED',
                'code': 'led_strip',
                'category': 'lighting',
                'description': 'Calcula metros de cinta LED y drivers necesarios',
                'input_parameters': {
                    'total_length': {'type': 'decimal', 'label': 'Longitud total (m)', 'required': True},
                    'power_per_meter': {'type': 'decimal', 'label': 'Potencia (W/m)', 'choices': [4.8, 9.6, 14.4]},
                    'voltage': {'type': 'choice', 'label': 'Voltaje', 'choices': ['12V', '24V']},
                    'strip_type': {'type': 'choice', 'label': 'Tipo', 'choices': ['SMD5050', 'SMD2835']}
                }
            },
            {
                'name': 'Calculadora de Cables',
                'code': 'cable',
                'category': 'lighting',
                'description': 'Calcula rollos de cable necesarios',
                'input_parameters': {
                    'total_length': {'type': 'decimal', 'label': 'Longitud (m)', 'required': True},
                    'wire_gauge': {'type': 'choice', 'label': 'Calibre', 'choices': ['12 AWG', '14 AWG']},
                    'cable_type': {'type': 'choice', 'label': 'Tipo', 'choices': ['THHN', 'Flexible']},
                    'installation_type': {'type': 'choice', 'label': 'Instalación', 'choices': ['Conduit', 'Directo']}
                }
            },
                        {
                'name': 'Calculadora de Empaste',
                'code': 'empaste',
                'category': 'construction',
                'description': 'Calcula cantidad de empaste necesario para superficie',
                'input_parameters': {
                    'area_to_cover': {'type': 'decimal', 'label': 'Área a empastar (m²)', 'required': True},
                    'empaste_type': {'type': 'choice', 'label': 'Tipo de empaste', 'choices': ['Interior', 'Exterior', 'Sellador']},
                    'number_of_coats': {'type': 'integer', 'label': 'Número de capas', 'default': 1},
                    'coverage_per_kg': {'type': 'decimal', 'label': 'Rendimiento (m²/kg)', 'default': 4.0}
                }
            },
            {
                'name': 'Calculadora de Perfiles',
                'code': 'profiles',
                'category': 'lighting',
                'description': 'Calcula perfiles de aluminio para iluminación LED',
                'input_parameters': {
                    'total_length': {'type': 'decimal', 'label': 'Longitud total (m)', 'required': True},
                    'profile_type': {'type': 'choice', 'label': 'Tipo de perfil', 'choices': ['Superficie', 'Empotrado', 'Suspendido', 'Esquina']},
                    'profile_size': {'type': 'choice', 'label': 'Tamaño', 'choices': ['16mm', '20mm', '25mm', '30mm']},
                    'finish_type': {'type': 'choice', 'label': 'Acabado', 'choices': ['Anodizado', 'Blanco', 'Negro', 'Plateado']},
                    'accessories_needed': {'type': 'choice', 'label': 'Accesorios', 'choices': ['Básicos', 'Completos'], 'default': 'Básicos'}
                }
            }
        ]
        
        for calc_data in calc_types:
            calc_type, created = CalculationType.objects.get_or_create(
                code=calc_data['code'],
                defaults=calc_data
            )
            if created:
                self.stdout.write(f'  ✓ Creada: {calc_type.name}')

    def _create_materials(self):
        """Crear materiales"""
        from apps.materials.models import Material, MaterialCategory
        
        self.stdout.write('🧱 Creando materiales...')
        
        # Obtener categorías
        cat_pintura = MaterialCategory.objects.get(name='Pintura y Acabados')
        cat_gypsum = MaterialCategory.objects.get(name='Drywall y Sistemas')
        cat_led = MaterialCategory.objects.get(name='Cintas y Tiras LED')
        cat_perfil = MaterialCategory.objects.get(name='Perfiles de Aluminio')
        cat_cable = MaterialCategory.objects.get(name='Cables Eléctricos')
        cat_driver = MaterialCategory.objects.get(name='Controladores y Drivers')
        
        materials = [
            # Pinturas
            {
                'name': 'Pintura Látex Interior Condor',
                'code': 'PINT-LAT-001',
                'category': cat_pintura,
                'description': 'Pintura látex lavable para interiores, acabado mate',
                'unit': 'galones',
                'yield_per_unit': Decimal('40.00'),
                'reference_price': Decimal('28.50'),
                'coverage_per_liter': Decimal('12.00')
            },
            {
                'name': 'Pintura Esmalte Sintético',
                'code': 'PINT-ESM-001',
                'category': cat_pintura,
                'unit': 'galones',
                'reference_price': Decimal('32.80'),
                'coverage_per_liter': Decimal('10.00')
            },
            
            # Gypsum
            {
                'name': 'Plancha Gypsum Standard 12.7mm',
                'code': 'GYPS-STD-127',
                'category': cat_gypsum,
                'description': 'Plancha 1.22x2.44m x 12.7mm',
                'unit': 'unidad',
                'yield_per_unit': Decimal('2.9768'),
                'reference_price': Decimal('12.50')
            },
            {
                'name': 'Perfil Metálico Parante 70mm',
                'code': 'GYPS-PERF-70',
                'category': cat_gypsum,
                'unit': 'unidad',
                'reference_price': Decimal('4.20')
            },
            
            # LED
            {
                'name': 'Cinta LED SMD5050 12V',
                'code': 'LED-5050-12V',
                'category': cat_led,
                'unit': 'metros',
                'reference_price': Decimal('8.50'),
                'power_per_meter': Decimal('14.40')
            },
            {
                'name': 'Cinta LED SMD2835 24V',
                'code': 'LED-2835-24V',
                'category': cat_led,
                'unit': 'metros',
                'reference_price': Decimal('12.80'),
                'power_per_meter': Decimal('9.60')
            },
            
            # Perfiles
            {
                'name': 'Perfil Aluminio Empotrar 17x7mm',
                'code': 'PERF-ALU-17x7',
                'category': cat_perfil,
                'unit': 'metros',
                'reference_price': Decimal('15.60')
            },
            
            # Cables
            {
                'name': 'Cable THHN 12AWG',
                'code': 'CABLE-THHN-12',
                'category': cat_cable,
                'unit': 'rollo',
                'reference_price': Decimal('85.50'),
                'wire_gauge': '12 AWG'
            },
            
            # Drivers
            {
                'name': 'Driver LED 60W 12V',
                'code': 'DRIVER-60W-12V',
                'category': cat_driver,
                'unit': 'unidad',
                'reference_price': Decimal('28.90')
            }
        ]
        
        for mat_data in materials:
            material, created = Material.objects.get_or_create(
                code=mat_data['code'],
                defaults=mat_data
            )
            if created:
                self.stdout.write(f'  ✓ Creado: {material.name}')

    def _create_suppliers(self):
        """Crear proveedores"""
        from apps.suppliers.models import Supplier
        from apps.materials.models import MaterialCategory
        
        self.stdout.write('🏪 Creando proveedores...')
        
        suppliers = [
            {
                'name': 'Ferretería El Constructor',
                'commercial_name': 'El Constructor',
                'supplier_type': 'materials',
                'contact_person': 'Carlos Mendoza',
                'phone': '+593987654321',
                'email': 'ventas@elconstructor.com.ec',
                'address': 'Av. 6 de Diciembre N26-89',
                'city': 'Quito',
                'zone': 'Centro Norte',
                'rating': 4,
                'payment_terms': '30 días',
                'is_preferred': True
            },
            {
                'name': 'LED Ecuador Lighting',
                'commercial_name': 'LED Ecuador',
                'supplier_type': 'lighting',
                'contact_person': 'Roberto Silva',
                'phone': '+593987123456',
                'email': 'ventas@ledecuador.com',
                'address': 'Av. Naciones Unidas E2-17',
                'city': 'Quito',
                'zone': 'Norte',
                'rating': 5,
                'is_preferred': True
            },
            {
                'name': 'ElectroAndina CIA. LTDA.',
                'commercial_name': 'ElectroAndina',
                'supplier_type': 'electrical',
                'contact_person': 'Miguel Torres',
                'phone': '+593987456123',
                'address': 'Av. América N39-17',
                'city': 'Quito',
                'zone': 'Norte',
                'rating': 4
            }
        ]
        
        for sup_data in suppliers:
            supplier, created = Supplier.objects.get_or_create(
                name=sup_data['name'],
                defaults=sup_data
            )
            if created:
                self.stdout.write(f'  ✓ Creado: {supplier.name}')
                
                # Asignar categorías
                if supplier.supplier_type == 'materials':
                    cats = MaterialCategory.objects.filter(category_type__in=['construction'])
                elif supplier.supplier_type == 'lighting':
                    cats = MaterialCategory.objects.filter(category_type='lighting')
                elif supplier.supplier_type == 'electrical':
                    cats = MaterialCategory.objects.filter(category_type='electrical')
                else:
                    cats = MaterialCategory.objects.all()
                
                supplier.categories.set(cats)

    def _create_projects(self):
        """Crear proyectos de ejemplo"""
        from apps.projects.models import Project
        
        self.stdout.write('🏗️ Creando proyectos...')
        
        projects = [
            {
                'name': 'Casa Moderna Cumbayá',
                'client': 'Juan Pérez Morales',
                'location': 'Cumbayá, Los Almendros',
                'project_type': 'residential',
                'description': 'Iluminación arquitectónica integral para casa de 280m²',
                'start_date': date(2025, 1, 15),
                'end_date': date(2025, 4, 30),
                'initial_budget': Decimal('12500.00'),
                'is_selected': True,
                'drive_folder_url': 'https://drive.google.com/drive/folders/casa-moderna-cumbaya'
            },
            {
                'name': 'Oficina Corporativa TechSolutions',
                'client': 'TechSolutions Ecuador S.A.',
                'location': 'Av. República del Salvador',
                'project_type': 'commercial',
                'description': 'Iluminación LED para oficinas de 450m²',
                'start_date': date(2025, 2, 1),
                'end_date': date(2025, 6, 15),
                'initial_budget': Decimal('18750.00'),
                'status': 'planning'
            },
            {
                'name': 'Villa Familiar Los Chillos',
                'client': 'Carlos Rodríguez',
                'location': 'Los Chillos, San Rafael',
                'project_type': 'residential',
                'start_date': date(2024, 8, 10),
                'end_date': date(2024, 12, 20),
                'actual_end_date': date(2024, 12, 18),
                'current_phase': 'completed',
                'status': 'completed',
                'initial_budget': Decimal('15200.00')
            }
        ]
        
        for proj_data in projects:
            project, created = Project.objects.get_or_create(
                name=proj_data['name'],
                defaults=proj_data
            )
            if created:
                self.stdout.write(f'  ✓ Creado: {project.name}')

    def _create_supplier_prices(self):
        """Crear precios de proveedores"""
        from apps.suppliers.models import Supplier, SupplierPrice
        from apps.materials.models import Material
        
        self.stdout.write('💰 Creando precios de proveedores...')
        
        # Precios El Constructor
        try:
            constructor = Supplier.objects.get(name='Ferretería El Constructor')
            pintura = Material.objects.get(code='PINT-LAT-001')
            
            SupplierPrice.objects.get_or_create(
                supplier=constructor,
                material=pintura,
                defaults={
                    'price': Decimal('28.50'),
                    'discount_percentage': Decimal('5.00'),
                    'valid_from': date.today(),
                    'is_current': True
                }
            )
            self.stdout.write(f'  ✓ Precio creado: {constructor.name} - {pintura.name}')
        except Exception as e:
            self.stdout.write(f'  ⚠️  Error creando precio: {e}')

    def _create_financial_summaries(self):
        """Crear resúmenes financieros"""
        from apps.budgets.models import ProjectFinancialSummary
        from apps.projects.models import Project
        
        self.stdout.write('📊 Creando resúmenes financieros...')
        
        for project in Project.objects.all():
            summary, created = ProjectFinancialSummary.objects.get_or_create(
                project=project
            )
            if created:
                summary.update_summary()
                self.stdout.write(f'  ✓ Resumen: {project.name}')

    def _print_summary(self):
        """Mostrar resumen final"""
        from apps.materials.models import MaterialCategory, Material
        from apps.suppliers.models import Supplier
        from apps.projects.models import Project
        from apps.calculations.models import CalculationType
        
        self.stdout.write('\n📊 RESUMEN FINAL:')
        self.stdout.write(f'  📂 Categorías: {MaterialCategory.objects.count()}')
        self.stdout.write(f'  🧱 Materiales: {Material.objects.count()}')
        self.stdout.write(f'  🏪 Proveedores: {Supplier.objects.count()}')
        self.stdout.write(f'  🏗️ Proyectos: {Project.objects.count()}')
        self.stdout.write(f'  🧮 Calculadoras: {CalculationType.objects.count()}')
        
        try:
            selected = Project.objects.get(is_selected=True)
            self.stdout.write(f'  ✅ Seleccionado: {selected.name}')
        except Project.DoesNotExist:
            self.stdout.write('  ⚠️  No hay proyecto seleccionado')
        
        self.stdout.write('\n🎉 ¡RegenerApp listo!')
        self.stdout.write('   🔗 Admin: http://127.0.0.1:8000/admin/')
        self.stdout.write('   🔗 APIs: http://127.0.0.1:8000/api/')
        self.stdout.write('   📱 Conecta ahora el Android!')